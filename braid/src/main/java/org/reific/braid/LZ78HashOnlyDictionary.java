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
	//int[] fullHashes = new int[CAPACITY];
	private int size;

	public LZ78HashOnlyDictionary() {
	}

	/**
	 * Lookup the entry with the given byte[] key, only considering the first 'length' elements of the key. 
	 * @return a list of possible matches, which can include zero or more false-positives.
	 */
	public int[] get(byte[] key, int length) {
		// compute hashcode of key
		int fullHashCode = 1;
		for (int i = 0; i < length; i++) {
			fullHashCode = 31 * fullHashCode + key[i];
		}
		// avoid negative
		int lowerBound = (fullHashCode & 0x7FFFFFFF) % CAPACITY;

		int numMatches = 0;
		// linear probe for free space 
		while (values[(lowerBound + numMatches) % CAPACITY] != 0) {
			numMatches++;
		}
		//System.out.println("num matches:" + numMatches);
		int[] result = new int[numMatches];
		for (int i = 0; i < result.length; i++) {
			// value offset by 1 so zero-initialized array means not-present
			result[i] = values[(lowerBound + i) % CAPACITY] - 1;
		}
		return result;
	}

	/**
	 * Store 'value' at the given byte[] key, only considering the first 'length' elements of the key. After calling, 
	 * callers must not modify any elements of the key within this range.
	 */
	public void put(byte[] key, int length, int value) {
		// compute hashcode of key
		int fullHashCode = 1;
		for (int i = 0; i < length; i++) {
			fullHashCode = 31 * fullHashCode + key[i];
		}
		// avoid negative
		int modedHashCode = (fullHashCode & 0x7FFFFFFF) % CAPACITY;

		// linear probe for free space 
		while (values[modedHashCode] != 0) {
			//System.out.println("collision");
			modedHashCode = (modedHashCode + 1) % CAPACITY;
		}
		// value offset by 1 so zero-initialized array means not-present
		//System.out.println(modedHashCode);
		values[modedHashCode] = value + 1;
		size = size + 1;

	}

}
