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

	int[] values;
	int[] fullHashes;
	private int numElements;
	private float loadFactor;

	private int threshold;

	protected void rehash() {
		int oldCapacity = values.length;
		int[] oldValues = values;
		int[] oldFullHashes = fullHashes;

		// overflow-conscious code
		int newCapacity = (oldCapacity << 1) + 1;
		if (newCapacity - MAX_ARRAY_SIZE > 0) {
			if (oldCapacity == MAX_ARRAY_SIZE)
				// Keep running with MAX_ARRAY_SIZE buckets
				return;
			newCapacity = MAX_ARRAY_SIZE;
		}
		values = new int[newCapacity];
		fullHashes = new int[newCapacity];

		threshold = (int) Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);

		for (int i = 0; i < oldCapacity; i++) {
			int oldValue = oldValues[i];
			if (oldValue != 0) {
				int index = oldFullHashes[i] % newCapacity;
				while (values[index] != 0) {
					index = (index + 1) % values.length;
				}
				values[index] = oldValue;
				fullHashes[index] = oldFullHashes[i];
			}
		}
	}

	public LZ78HashOnlyDictionary(int initialCapacity, float loadFactor) {
		values = new int[initialCapacity];
		fullHashes = new int[initialCapacity];
		this.loadFactor = loadFactor;
		this.threshold = (int)Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
	}

	/**
	 * Lookup the entry with the given byte[] key, only considering the first 'length' elements of the key. 
	 * @return a list of possible matches, which can include zero or more false-positives.
	 */
	public int[] get(byte[] key, int length) {
		int hashCode = computeHashCode(key, length);
		int lowerBound = hashCode % values.length;
		//System.out.printf("get: %d\n", lowerBound);

		int numMatches = 0;
		int actualMatches = 0;
		// linear probe for free space 
		while (values[(lowerBound + numMatches) % values.length] != 0) {
			if (fullHashes[(lowerBound + numMatches) % values.length] == hashCode) {
				actualMatches++;
			}
			numMatches++;
		}
		//System.out.println("num matches:" + numMatches);
		int[] result = new int[actualMatches];
		int index = 0;
		for (int i = 0; i < numMatches; i++) {
			if (fullHashes[(lowerBound + i) % values.length] == hashCode) {
				// value offset by 1 so zero-initialized array means not-present
				result[index++] = values[(lowerBound + i) % values.length] - 1;
			}
		}
		return result;
	}

	private static int computeHashCode(byte[] key, int length) {
		int hashCode = 1;
		for (int i = 0; i < length; i++) {
			hashCode = 31 * hashCode + key[i];
		}
		// prevent negative
		// TODO keep full hashcode until later to prevent more false negatives
		hashCode = (hashCode & 0x7FFFFFFF);
		return hashCode;
	}

	/**
	 * Store 'value' at the given byte[] key, only considering the first 'length' elements of the key. After calling, 
	 * callers must not modify any elements of the key within this range.
	 */
	public void put(byte[] key, int length, int value) {
		if (numElements >= threshold) {
			rehash();
		}
		int hashCode = computeHashCode(key, length);
		int location = hashCode % values.length;
		//System.out.printf("put: %d %d\n", hashCode, location);

		// linear probe for free space 
		while (values[location] != 0) {
			//System.out.println("collision");
			location = (location + 1) % values.length;
		}
		// value offset by 1 so zero-initialized array means not-present
		//System.out.println(modedHashCode);
		values[location] = value + 1;
		fullHashes[location] = hashCode;
		numElements = numElements + 1;

	}

}