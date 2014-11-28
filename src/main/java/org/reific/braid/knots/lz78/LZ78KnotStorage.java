package org.reific.braid.knots.lz78;

import java.util.HashMap;
import java.util.Map;

import org.reific.braid.KnotStorage;

/**
 * 
 * Example of storing: this that the other
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
 * TODO: encode record lengths as vInts (see lucene file format for definition)
 * (instead of fixed 32 bit int)
 * <p>
 * TODO: encode prefix pointers as negative offset vInts (instead of fixed 32
 * bit int)
 * <p>
 * TODO: investigate if there is a better data structure than Map<String,
 * Integer> to store the working dictionary.
 * <p>
 * 
 */
public class LZ78KnotStorage implements KnotStorage {

	private static final int INITIAL_BYTE_BUFFER_SIZE = 128;
	private final AutoGrowingByteBuffer byteBuffer = new AutoGrowingByteBuffer(INITIAL_BYTE_BUFFER_SIZE);
	private final Map<String, Integer> dictionary = new HashMap<String, Integer>();

	public LZ78KnotStorage() {
	}

	@Override
	public int store(final String s) {
		int startingBufferPosition = byteBuffer.nextWritePosition();
		int stringLength = s.length();
		StringBuilder currentPhrase = new StringBuilder();
		int currentPointer = -1;

		byteBuffer.putInt(stringLength);

		for (int i = 0; i < stringLength; i++) {
			int bufferPosition = byteBuffer.nextWritePosition();
			char token = s.charAt(i);
			currentPhrase.append(token);
			Integer pointer = dictionary.get(currentPhrase);

			if (pointer == null) {
				// not found in dictionary. Add to dictionary and compressed
				// stream and start over
				byteBuffer.putChar(token);
				byteBuffer.putInt(currentPointer);
				dictionary.put(currentPhrase.toString(), bufferPosition);
				currentPhrase = new StringBuilder();
				currentPointer = -1;
			} else if (i == stringLength - 1) {
				// found in dictionary, but at the end of the input string.
				// Add to the output stream
				byteBuffer.putChar(token);
				byteBuffer.putInt(currentPointer);
				currentPhrase = new StringBuilder();
				currentPointer = -1;
			} else {
				// found in dictionary. keep parsing to match a longer
				// dictionary entry
				currentPointer = pointer;
			}
		}
		return startingBufferPosition;
	}

	@Override
	public String lookup(int index) {
		int sizeOfString = byteBuffer.getInt(index);
		StringBuilder result = new StringBuilder(sizeOfString);
		index = index + 4;
		// walk forward over the current string
		while (result.length() < sizeOfString) {
			String result2 = "";
			int pointer = index;
			// follow the pointer backwards to find tokens
			while (pointer > 0) {
				char character = byteBuffer.getChar(pointer);
				pointer = byteBuffer.getInt(pointer + 2);
				result2 = character + result2;
			}
			result.append(result2);
			index += 6;
		}
		return result.toString();
	}
}
