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
		byte[] currentPhrase1 = new byte[stringLength];
		int currentPhraseLength = 0;
		// 0 is used as the terminating code instead of -1, since storing negative numbers in VInt form would be expensive. 
		// This doesn't affect the external int braid location pointers or the indexes into the byteBuffer. 
		// Zero is still a valid location (which will contain the length of the first record)
		int currentPointer = 0;

		byteBuffer.putVInt(stringLength);

		for (int i = 0; i < stringLength; i++) {
			int bufferPosition = byteBuffer.nextWritePosition();
			byte token = stringBytes[i];
			currentPhrase1[currentPhraseLength++] = token;
			int pointer = dictionaryGet(currentPhrase1, currentPhraseLength);

			if (pointer == -1) {
				// not found in dictionary. Add to dictionary and compressed
				// stream and start over
				byteBuffer.putByte(token);
				byteBuffer.putVInt(currentPointer);
				dictionaryPut(currentPhrase1, currentPhraseLength, bufferPosition);
				currentPhrase1 = new byte[stringLength];
				currentPhraseLength = 0;
				currentPointer = 0;
			} else if (i == stringLength - 1) {
				// found in dictionary, but at the end of the input string.
				// Add to the output stream
				byteBuffer.putByte(token);
				byteBuffer.putVInt(currentPointer);
				currentPhrase1 = new byte[stringLength];
				currentPhraseLength = 0;
				currentPointer = 0;
			} else {
				// found in dictionary. keep parsing to match a longer
				// dictionary entry
				currentPointer = pointer;
			}
		}
		return startingBufferPosition;
	}

	private void dictionaryPut(byte[] string, int stringLength, int value) {
		dictionary.put(string, stringLength, value);
	}

	private int dictionaryGet(byte[] string, int length) {
		int[] possibleIndexes = dictionary.get(string, length);
		if (possibleIndexes.length == 0) {
			return -1;
		}
		for (int i = 0; i < possibleIndexes.length; i++) {
			byte[] possibleMatch = lookupPointer(possibleIndexes[i], length);
			if (possibleMatch == null) {
				continue;
			}
			//Key key = new LZ78Dictionary.Key(string, length);
			//Key key2 = new LZ78Dictionary.Key(possibleMatch, possibleMatch.length);
			if (equals(string, length, possibleMatch, possibleMatch.length)) {
				return possibleIndexes[i];
			}
		}
		return -1;
	}

	private static boolean equals(byte[] a, int aLength, byte[] b, int bLength) {
		if (aLength != bLength) {
			return false;
		}
		for (int i = 0; i < aLength; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * lookup the given index in the compressed buffer for a string of the given length. 
	 * 
	 * In a traditional LZ78 implementation this would be accomplished by looking up
	 * directly in the dictionary, but our dictionary only contains hashes of keys (i.e., potential matches), not the actual keys.
	 * 
	 * @param index to a location in the compressed buffer. This must be a valid index, or behavior is undefined.
	 * @param expectedLength of the string being looked up. If the string found at the given index is of a different length, null will be returned.
	 * 
	 * @return the string at the given index unless it is not of the expected length, in which case null is returned.
	 */
	private byte[] lookupPointer(int index, int expectedLength) {
		byte[] result = new byte[expectedLength];
		int resultCount = 0;
		while (index > 0) {
			if (resultCount >= expectedLength) {
				// found a match longer that the expectedLength. Not a match.
				return null;
			}
			byte character = byteBuffer.getByte(index);
			long nextVInt = byteBuffer.getVInt(index + 1);
			// take the low-order int
			index = (int) nextVInt;
			result[result.length - 1 - resultCount++] = character;
		}
		if (resultCount < expectedLength) {
			// found a match shorter that the expectedLength. Not a match.
			return null;
		}
		return result;
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
