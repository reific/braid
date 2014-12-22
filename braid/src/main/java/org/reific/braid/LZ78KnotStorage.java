/*
    Braid - Library for Transparent Compression of Java Strings.

    Copyright (C) 2014 James Scriven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.reific.braid;

import java.nio.charset.Charset;

/**
 * (With VInt) Example of storing: this that the other
 * <p>
 * 
 * <pre>
 * byte# | value, prefixIndex | representedSubstring
 * 
 * 0  |19  | <length of uncompressed braid in characters>
 * 1  |t, 0| t
 * 4  |h, 0| h
 * 7  |i, 0| i
 * 10 |s, 0| s
 * 13 | , 0| _
 * 16 |h, 1| th
 * 19 |a, 0| a
 * 22 | , 1| t_
 * 25 |e,16| the
 * 28 |o,13| _o
 * 31 |r,25| ther
 * 
 * </pre>
 * 
 * (Without VInt) Example of storing: this that the other
 * <p>
 * 
 * <pre>
 * byte# | value, prefixIndex | representedSubstring
 * 
 * 0  |19  | <length of uncompressed braid in characters>
 * 4  |t,-1| t
 * 10 |h,-1| h
 * 16 |i,-1| i
 * 22 |s,-1| s
 * 28 | ,-1| _
 * 34 |h, 4| th
 * 40 |a,-1| a
 * 46 | , 4| t_
 * 52 |e,34| the
 * 58 |o,28| _o
 * 64 |r,52| ther
 * 
 * </pre>
 * 
 * <p>
 * TODO: encode prefix pointers as negative offset vInts (instead of fixed 32
 * bit int)
 * <p>
 * 
 */
class LZ78KnotStorage implements KnotStorage {

	private static final Charset STRING_CHARSET = Charset.forName("UTF-8");

	private final Buffer byteBuffer;
	private LZ78HashOnlyDictionary dictionary;
	private final int initialDictionaryCapacity;
	private final float dictionaryLoadFactor;

	public LZ78KnotStorage(Buffer buffer, int initialDictionaryCapacity, float dictionaryLoadFactor) {
		this.byteBuffer = buffer;
		this.initialDictionaryCapacity = initialDictionaryCapacity;
		this.dictionaryLoadFactor = dictionaryLoadFactor;
		dictionary = new LZ78HashOnlyDictionary(initialDictionaryCapacity, dictionaryLoadFactor);
	}

	@Override
	public void flush() {
		dictionary = new LZ78HashOnlyDictionary(initialDictionaryCapacity, dictionaryLoadFactor);
	}

	@Override
	public long spaceUsed() {
		return byteBuffer.getSize() + dictionary.spaceUsed();
	}

	@Override
	public int store(final String string) {
		int startingBufferPosition = byteBuffer.nextWritePosition();
		byte[] stringBytes = string.getBytes(STRING_CHARSET);
		int stringLength = stringBytes.length;

		// 0 is used as the terminating code instead of -1, since storing negative numbers in VInt form would be expensive. 
		// This doesn't affect the external int braid location pointers or the indexes into the byteBuffer. 
		// Zero is still a valid location (which will contain the length of the first record)

		byteBuffer.putVInt(stringLength);

		int i = 0;
		while (i < stringLength) {
			int bufferPosition = byteBuffer.nextWritePosition();
			// walk forward over string lengths for probable dictionary matches
			int length = 1;
			int[] possibleIndexes = null;
			int[] nextPossibleIndexes = dictionary.get(stringBytes, i, length);
			// ignore the final element of stringBytes
			for (length = 2; length < (stringLength - i + 1) && nextPossibleIndexes.length > 0; length++) {
				possibleIndexes = nextPossibleIndexes;
				nextPossibleIndexes = dictionary.get(stringBytes, i, length);
			}
			// readjust
			length = length - 2;
			// 
			// possibleIndexes will be null or non-empty now
			int confirmedIndex = -1;
			if (possibleIndexes != null) {
				confirmedIndex = confirmIndex(possibleIndexes, stringBytes, i, length--);
				while (confirmedIndex == -1 && length > 0) {
					possibleIndexes = dictionary.get(stringBytes, i, length);
					confirmedIndex = confirmIndex(possibleIndexes, stringBytes, i, length--);
				}
				// readjust
				length++;
			}
			byteBuffer.putByte(stringBytes[i + length]);
			byteBuffer.putVInt(confirmedIndex == -1 ? 0 : confirmedIndex);
			if (i + length + 1 != stringLength) {
				dictionaryPut(stringBytes, i, length + 1, bufferPosition);
			}
			i += length + 1;
		}
		return startingBufferPosition;
	}

	private void dictionaryPut(byte[] string, int offset, int stringLength, int value) {
		dictionary.put(string, offset, stringLength, value);
	}

	private int confirmIndex(int[] possibleIndexes, byte[] string, int offset, int length) {

		forloop: for (int i = 0; i < possibleIndexes.length; i++) {
			int possibleIndex = possibleIndexes[i];
			int resultCount = 0;
			while (possibleIndex > 0) {
				if (resultCount >= length) {
					// found a match longer that the expectedLength. Not a match.
					continue forloop;
				}
				byte character = byteBuffer.getByte(possibleIndex);
				if (string[offset + length - 1 - resultCount++] != character) {
					continue forloop;
				}
				long nextVInt = byteBuffer.getVInt(possibleIndex + 1);
				// take the low-order int
				possibleIndex = (int) nextVInt;
			}
			if (resultCount < length) {
				// found a match shorter that the expectedLength. Not a match.
				continue forloop;
			}
			return possibleIndexes[i];
		}
		return -1;
	}

	@Override
	public String lookup(int index) {
		final long sizeOfStringVInt = byteBuffer.getVInt(index);
		final int sizeOfString = (int) sizeOfStringVInt;
		final int sizeOfStringbytesUsed = (int) (sizeOfStringVInt >> 32);
		// take low-order byte
		byte[] result = new byte[sizeOfString];
		byte[] innerResult = new byte[sizeOfString];
		int resultCount = 0;
		index = index + sizeOfStringbytesUsed;
		// walk forward over the current string
		while (resultCount < sizeOfString) {
			int innerResultCount = 0;
			int pointer = index;
			// follow the pointer backwards to find tokens
			while (pointer > 0) {
				byte character = byteBuffer.getByte(pointer);
				long nexVIntPointer = byteBuffer.getVInt(pointer + 1);
				final int nextPointer = (int) nexVIntPointer;
				final int nextPointerBytesUsed = (int) (nexVIntPointer >> 32);
				if (pointer == index) {
					//first time through inner loop. prepare index for next iteration of outer loop
					index += 1;
					index += nextPointerBytesUsed;
				}
				pointer = nextPointer;
				innerResult[innerResultCount++] = character;
			}
			for (int i = innerResultCount - 1; i >= 0; i--) {
				result[resultCount++] = innerResult[i];
			}
		}
		return new String(result, STRING_CHARSET);
	}
}
