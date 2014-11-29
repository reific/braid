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
package org.reific.braid.knots.lz78;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 *
 */
public class LZ78Dictionary {

	TObjectIntMap<Key> dictionary;

	/**
	 * HashMap key wrapper for byte[]. Only the specified length is considered for hashCode/Equals so that
	 * arrays can be reused by clients for sub-arrays.
	 */
	static class Key {

		final byte[] currentPhrase;
		final private int length;

		public Key(byte[] currentPhrase, int length) {
			this.currentPhrase = currentPhrase;
			this.length = length;
		}

		@Override
		public boolean equals(Object other) {
			Key otherKey = ((Key) other);
			if (otherKey.length != length) {
				return false;
			}

			for (int i = 0; i < length; i++) {
				if (currentPhrase[i] != otherKey.currentPhrase[i]) {
					return false;
				}
			}

			return true;

		}

		@Override
		public int hashCode() {
			int result = 1;
			for (int i = 0; i < length; i++) {
				result = 31 * result + currentPhrase[i];
			}
			return result;
		}
	}

	public LZ78Dictionary(int initialDictionarySize, float dictionaryLoadFactor, int noEntryValue) {
		dictionary = new TObjectIntHashMap<Key>(initialDictionarySize, dictionaryLoadFactor, noEntryValue);
	}

	/**
	 * Lookup the entry with the given byte[] key, only considering the first 'length' elements.
	 */
	public int get(byte[] key, int length) {
		return dictionary.get(new Key(key, length));
	}

	/**
	 * Store 'value' at the given byte[] key, only considering the first 'length' elements of the key. After calling, 
	 * callers must not modify any elements of the key within this range.
	 */
	public void put(byte[] key, int length, int value) {
		dictionary.put(new Key(key, length), value);
		
	}



}
