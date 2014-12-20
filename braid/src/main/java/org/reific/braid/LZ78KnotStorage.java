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
import java.util.Objects;

import org.reific.braid.LZ78Dictionary.Key;

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
	private final LZ78HashOnlyDictionary dictionary = new LZ78HashOnlyDictionary(64, 0.5f);

	public LZ78KnotStorage(Buffer buffer) {
		this.byteBuffer = buffer;
	}

	@Override
	public int getCompressedSize() {
		return byteBuffer.getSize();
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

			if (pointer == -2) {
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

	private void dictionaryPut(byte[] currentPhrase1, int currentPhraseLength, int bufferPosition) {
		dictionary.put(currentPhrase1, currentPhraseLength, bufferPosition);
	}

	private int dictionaryGet(byte[] currentPhrase, int currentPhraseLength) {
		int[] possibleIndexes = dictionary.get(currentPhrase, currentPhraseLength);
		if (possibleIndexes.length == 0) {
			return -2;
		}
		for (int i = 0; i < possibleIndexes.length; i++) {
			byte[] possibleMatch = lookupPointer(possibleIndexes[i], currentPhraseLength);
			if (possibleMatch == null) {
				continue;
			}
			Key key = new LZ78Dictionary.Key(currentPhrase, currentPhraseLength);
			Key key2 = new LZ78Dictionary.Key(possibleMatch, possibleMatch.length);
			if (Objects.equals(key, key2)) {
				return possibleIndexes[i];
			}
		}
		return -2;
		//throw new RuntimeException();

	}

	private byte[] lookupPointer(int index, int length) {
		byte[] innerResult = new byte[length];
		int innerResultCount = 0;
		while (index > 0) {
			if (innerResultCount >= innerResult.length) {
				// request of wrong length requested. no match.
				return null;
			}
			byte character = byteBuffer.getByte(index);
			VInt nexVInt = byteBuffer.getVInt(index + 1);

			index = nexVInt.value;
			innerResult[innerResult.length - 1 - innerResultCount++] = character;
		}
		if (innerResultCount != length) {
			return null;
		}
		return innerResult;
	}

	@Override
	public String lookup(int index) {
		VInt sizeOfString = byteBuffer.getVInt(index);
		byte[] result = new byte[sizeOfString.value];
		byte[] innerResult = new byte[sizeOfString.value];
		int resultCount = 0;
		index = index + sizeOfString.numBytes;
		// walk forward over the current string
		while (resultCount < sizeOfString.value) {
			int innerResultCount = 0;
			int pointer = index;
			// follow the pointer backwards to find tokens
			while (pointer > 0) {
				byte character = byteBuffer.getByte(pointer);
				VInt nexVInt = byteBuffer.getVInt(pointer + 1);
				if (pointer == index) {
					//first time through inner loop. prepare index for next iteration of outer loop
					index += 1;
					index += nexVInt.numBytes;
				}
				pointer = nexVInt.value;
				innerResult[innerResultCount++] = character;
			}
			for (int i = innerResultCount - 1; i >= 0; i--) {
				result[resultCount++] = innerResult[i];
			}
		}
		return new String(result, STRING_CHARSET);
	}
}
