package org.reific.braid.knots.lz78;

import java.util.HashMap;
import java.util.Map;

import org.reific.braid.KnotStorage;

/**
 * <pre>
 * 
 * this that the other
 * 
 * 0  |19  |
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
 */
public class LZ78KnotStorage implements KnotStorage {

	private static final int INITIAL_BYTE_BUFFER_SIZE = 2;
	private final AutoGrowingByteBuffer byteBuffer = new AutoGrowingByteBuffer(
			INITIAL_BYTE_BUFFER_SIZE);
	private final Map<String, Integer> dictionary = new HashMap<String, Integer>();
	private int currentPointer = -1;
	private String currentPhrase;

	public LZ78KnotStorage() {

		dictionary.put("", -1);
		currentPhrase = "";
	}

	@Override
	public int store(String s) {
		int startingBufferPosition = byteBuffer.nextWritePosition();
		int bufferPosition;

		byteBuffer.putInt(s.length());

		for (int i = 0; i < s.length(); i++) {
			bufferPosition = byteBuffer.nextWritePosition();
			char token = s.charAt(i);
			Integer pointer = dictionary.get(currentPhrase + token);
			if (pointer == null) {

				// maybe put token/size/pointer in order?
				byteBuffer.putChar(token);
				byteBuffer.putInt(currentPointer);
				dictionary.put(currentPhrase + token, bufferPosition);
				currentPhrase = "";
				currentPointer = -1;

			} else if (i + 1 == s.length()) {
				// there was a dictionary match at the end of the string,
				// which has
				// not yet been stored (potentially this could be delayed
				// until
				// lookup was called, which might improve compression)
				byteBuffer.putChar(token);
				byteBuffer.putInt(currentPointer);
				currentPhrase = "";
				currentPointer = -1;

			} else {
				currentPhrase = currentPhrase + token;
				currentPointer = pointer;

			}
		}
		return startingBufferPosition;
	}

	@Override
	public String lookup(int index) {
		String result = "";
		int sizeOfString = byteBuffer.getInt(index);
		for (int i = 0; result.length() < sizeOfString; i++) {
			result += lookupInternal(index + 4 + (i * 6));
		}

		return result;

	}

	private String lookupInternal(int index) {
		String result = "";
		int pointer = index;
		while (pointer > 0) {
			char character = byteBuffer.getChar(pointer);
			pointer = byteBuffer.getInt(pointer + 2);
			result = character + result;
		}
		return result;
	}

}
