package org.reific.braid.knots.lz78;

import java.util.HashMap;
import java.util.Map;

import org.reific.braid.KnotStorage;

/**
 * 
 */
public class LZ78KnotStorage implements KnotStorage {

	private static final int INITIAL_BYTE_BUFFER_SIZE = 2;
	private final AutoGrowingByteBuffer byteBuffer = new AutoGrowingByteBuffer(
			INITIAL_BYTE_BUFFER_SIZE);
	private final Map<String, Integer> dictionary = new HashMap<String, Integer>();
	private final String[] reverseDictionary = new String[1000];
	private int currentPointer = 0;
	private int nextNewPointer;
	private String currentPhrase;
	private int bufferPosition;

	public LZ78KnotStorage() {

		dictionary.put("", currentPointer);
		reverseDictionary[currentPointer] = "";
		nextNewPointer++;
		currentPhrase = "";
	}

	@Override
	public int store(String s) {
		bufferPosition = byteBuffer.nextWritePosition();

		byteBuffer.putInt(s.length());
		for (int i = 0; i < s.length(); i++) {
			char token = s.charAt(i);
			Integer pointer = dictionary.get(currentPhrase + token);
			if (pointer == null) {

				// maybe put token/size/pointer in order?
				byteBuffer.putChar(token);
				byteBuffer.putInt(currentPointer);
				dictionary.put(currentPhrase + token, nextNewPointer);
				reverseDictionary[nextNewPointer] = currentPhrase + token;
				System.out.printf("Storing: %s <%s_%s> [%s]=%s\n",
						currentPointer, currentPhrase, token, currentPhrase
								+ token, nextNewPointer);

				nextNewPointer++;
				currentPhrase = "";
				currentPointer = 0;

			} else if (i + 1 == s.length()) {
				// there was a dictionary match at the end of the string,
				// which has
				// not yet been stored (potentially this could be delayed
				// until
				// lookup was called, which might improve compression)
				byteBuffer.putChar(token);
				byteBuffer.putInt(currentPointer);
				currentPhrase = "";
				currentPointer = 0;

			} else {
				currentPhrase = currentPhrase + token;
				currentPointer = pointer;

			}
		}
		return bufferPosition;
	}

	@Override
	public String lookup(int index) {
		int sizeOfString = byteBuffer.getInt(index);
		index += 4;
		String result = "";
		for (int i = 0; result.length() < sizeOfString; i++) {
			char character = byteBuffer.getChar(index + (i * 6));
			int pointer = byteBuffer.getInt(index + (i * 6) + 2);
			System.out.printf("Found: <%s> %s\n", character, pointer);
			result += reverseDictionary[pointer] + character;
		}
		return result;
	}

}
