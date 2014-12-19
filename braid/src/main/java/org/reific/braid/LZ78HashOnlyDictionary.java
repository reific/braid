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

	private static final int CAPACITY = 123456;
	int[] values = new int[CAPACITY];
	int[] fullHashes = new int[CAPACITY];
	private int numElements;

	public LZ78HashOnlyDictionary() {
	}

	/**
	 * Lookup the entry with the given byte[] key, only considering the first 'length' elements of the key. 
	 * @return a list of possible matches, which can include zero or more false-positives.
	 */
	public int[] get(byte[] key, int length) {
		int hashCode = computeHashCode(key, length);
		int lowerBound = hashCode % CAPACITY;
		//System.out.printf("get: %d\n", lowerBound);

		int numMatches = 0;
		int actualMatches = 0;
		// linear probe for free space 
		while (values[(lowerBound + numMatches) % CAPACITY] != 0) {
			if (fullHashes[(lowerBound + numMatches) % CAPACITY] == hashCode) {
				actualMatches++;
			}
			numMatches++;
		}
		//System.out.println("num matches:" + numMatches);
		int[] result = new int[actualMatches];
		int index = 0;
		for (int i = 0; i < numMatches; i++) {
			if (fullHashes[(lowerBound + i) % CAPACITY] == hashCode) {
				// value offset by 1 so zero-initialized array means not-present
				result[index++] = values[(lowerBound + i) % CAPACITY] - 1;
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
		int hashCode = computeHashCode(key, length);
		int location = hashCode % CAPACITY;
		//System.out.printf("put: %d %d\n", hashCode, location);

		// linear probe for free space 
		while (values[location] != 0) {
			//System.out.println("collision");
			location = (location + 1) % CAPACITY;
		}
		// value offset by 1 so zero-initialized array means not-present
		//System.out.println(modedHashCode);
		values[location] = value + 1;
		fullHashes[location] = hashCode;
		numElements = numElements + 1;

	}

}
