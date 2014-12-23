/*
    Seam - Library for Transparent Compression of Java Strings.

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

class LZ78HashOnlyDictionary {

	/**
	 * The maximum size of array to allocate.
	 * Some VMs reserve some header words in an array.
	 * Attempts to allocate larger arrays may result in
	 * OutOfMemoryError: Requested array size exceeds VM limit
	 * (from java.utl.Hashtable)
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	private static final int[] EMPTY_RESULT = new int[0];

	private int capacity;
	private int[] values;
	private int[] fullHashes;
	private int numElements;
	private float loadFactor;
	private int threshold;

	LZ78HashOnlyDictionary(int initialCapacity, float loadFactor) {
		capacity = initialCapacity;
		values = new int[initialCapacity];
		fullHashes = new int[initialCapacity];
		this.loadFactor = loadFactor;
		this.threshold = (int) Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
	}

	protected void rehash() {
		int oldCapacity = values.length;
		int[] oldValues = values;
		int[] oldFullHashes = fullHashes;

		// overflow-conscious code (duplicated from java.util.Hashtable)
		int newCapacity = (oldCapacity << 1) + 1;
		if (newCapacity - MAX_ARRAY_SIZE > 0) {
			if (oldCapacity == MAX_ARRAY_SIZE) {
				// Keep running with MAX_ARRAY_SIZE buckets
				return;
			}
			newCapacity = MAX_ARRAY_SIZE;
		}
		capacity = newCapacity;
		values = new int[newCapacity];
		fullHashes = new int[newCapacity];

		threshold = (int) Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
		for (int i = 0; i < oldCapacity; i++) {
			int oldValue = oldValues[i];
			if (oldValue != 0) {
				// masked to prevent negative
				int index = (oldFullHashes[i] & 0x7FFFFFFF) % newCapacity;
				while (values[index] != 0) {
					index = (index + 1) % values.length;
				}
				values[index] = oldValue;
				fullHashes[index] = oldFullHashes[i];
			}
		}
	}

	/**
	 * Lookup the entry with the given byte[] key, only considering the first 'length' elements of the key. 
	 * @return a list of possible matches, which can include zero or more false-positives.
	 * 
	 */
	public int[] get(byte[] key, int offset, int length) {
		int hashCode = computeHashCode(key, offset, length);
		// masked to prevent negative
		int lowerBound = (hashCode & 0x7FFFFFFF) % capacity;
		int upperBound = lowerBound;
		int actualMatches = 0;
		// linear probe for free space 
		while (values[upperBound % capacity] != 0) {
			if (fullHashes[upperBound % capacity] == hashCode) {
				actualMatches++;
			}
			upperBound++;
		}
		if (upperBound == lowerBound) {
			return EMPTY_RESULT;
		}
		int numMatches = upperBound - lowerBound;

		int[] result = new int[actualMatches];
		int resultIndex = 0;
		for (int i = 0; i < numMatches; i++) {
			if (fullHashes[(lowerBound + i) % capacity] == hashCode) {
				// value offset by 1 so zero-initialized array means not-present
				result[resultIndex++] = values[(lowerBound + i) % capacity] - 1;
			}
		}
		return result;
	}

	//	public boolean possiblyExists(byte[] key, int offset, int length) {
	//		int hashCode = computeHashCode(key, offset, length);
	//		// masked to prevent negative
	//		int lowerBound = (hashCode & 0x7FFFFFFF);
	//		if (lowerBound >= capacity) {
	//			lowerBound = lowerBound % capacity;
	//		}
	//		int upperBound = lowerBound;
	//		// linear probe for free space 
	//		while (values[upperBound] != 0) {
	//			if (fullHashes[upperBound] == hashCode) {
	//				return true;
	//			}
	//			upperBound++;
	//			if (upperBound >= capacity) {
	//				upperBound = upperBound % capacity;
	//			}
	//			else if (upperBound < 0) {
	//				upperBound += capacity;
	//			}
	//		}
	//		return false;
	//	}

	final int indexOfLongestPossiblePrefix(final byte[] key, final int offset, final int length) {
		int hashCode = 1;
		int indexFound = -1;
		final int upperBound = length + offset;
		forloop:
		for (int i = offset; i < upperBound; i++) {
			hashCode = 31 * hashCode + key[i];
			// masked to prevent negative
			int index = hashCode & 0x7FFFFFFF;
			if (index >= capacity) {
				index = index % capacity;
			}
			while (values[index] != 0) {
				if (fullHashes[index] == hashCode) {
					indexFound = i;
					continue forloop;
				}
				index++;
				if (index >= capacity) {
					index = index % capacity;
				}
				//integer overflow
				else if (index < 0) {
					index += capacity;
				}
			}
			if (indexFound == -1) {
				return -1;
			}
		}
		return indexFound;
	}

	private static int computeHashCode(final byte[] key, final int offset, final int length) {
		int hashCode = 1;
		final int upperBound = length + offset;
		for (int i = offset; i < upperBound; i++) {
			hashCode = 31 * hashCode + key[i];
		}
		return hashCode;
	}

	/**
	 * Store 'value' at the given byte[] key, only considering the first 'length' elements of the key. After calling, 
	 * callers must not modify any elements of the key within this range.
	 */
	public void put(byte[] key, int offset, int length, int value) {
		if (numElements >= threshold) {
			rehash();
		}
		int hashCode = computeHashCode(key, offset, length);
		// masked to prevent negative
		int location = (hashCode & 0x7FFFFFFF) % values.length;

		// linear probe for free space 
		while (values[location] != 0) {
			location = (location + 1) % values.length;
		}
		// value offset by 1 so zero-initialized array means not-present
		values[location] = value + 1;
		fullHashes[location] = hashCode;
		numElements = numElements + 1;
	}

	public long spaceUsed() {
		return (values.length + fullHashes.length) * 4;
	}

}
