package org.reific.braid.knots;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reific.braid.KnotStorage;

/**
 * 
 */
public class LZ78KnotStorage implements KnotStorage {

	private static final int INITIAL_BYTE_BUFFER_SIZE = 128;
	// ordered list of ByteBuffers of increasing size.
	// data[n+1].length = 2 * data[n].length
	private final List<ByteBuffer> data = new ArrayList<ByteBuffer>();
	private final Map<String, Integer> dictionary = new HashMap<String, Integer>();
	private final String[] reverseDictionary = new String[1000];
	private int currentPointer = 0;
	private int nextNewPointer;
	private String currentPhrase;
	private int bufferPosition;

	public LZ78KnotStorage() {
		ByteBuffer buffer = ByteBuffer.allocate(INITIAL_BYTE_BUFFER_SIZE);
		dictionary.put("", currentPointer);
		reverseDictionary[currentPointer] = "";
		nextNewPointer++;
		currentPhrase = "";

		data.add(buffer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int store(String s) {
		int currentByteBufferNumber = 0;
		ByteBuffer byteBuffer = data.get(currentByteBufferNumber);
		bufferPosition = byteBuffer.position();

		byteBuffer.putInt(s.length());
		for (int i = 0; i < s.length(); i++) {
			char token = s.charAt(i);
			Integer pointer = dictionary.get(currentPhrase + token);
			if (pointer == null) {

				// maybe put token/size/pointer in order?

				byteBuffer.putChar(token).putInt(currentPointer);
				// I THINK this is the right key
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
				byteBuffer.putChar(token).putInt(currentPointer);
				currentPhrase = "";
				currentPointer = 0;

			} else {
				currentPhrase = currentPhrase + token;
				currentPointer = pointer;

			}
		}

		// TODO handle null and empty string inputs
		return bufferPosition;
	}

	@Override
	public String lookup(int index) {
		// TODO get correct bytebuffer
		int currentByteBufferNumber = 0;
		ByteBuffer byteBuffer = data.get(currentByteBufferNumber);
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
