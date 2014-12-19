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

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.UnsupportedEncodingException;

class LZ78Dictionary {

	TObjectIntMap<Key> dictionary;

	/**
	 * HashMap key wrapper for byte[]. Only the specified length is considered for hashCode/Equals so that
	 * arrays can be reused by clients for sub-arrays.
	 */
	public static class Key {

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

		@Override
		public String toString() {
			try {
				return "_" + new String(currentPhrase, 0, length, "UTF-8") + "_";
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}

	public LZ78Dictionary(int initialDictionarySize, float dictionaryLoadFactor, int noEntryValue) {
		dictionary = new TObjectIntHashMap<Key>(initialDictionarySize, dictionaryLoadFactor, noEntryValue);
	}

	/**
	 * Lookup the entry with the given byte[] key, only considering the first 'length' elements.
	 */
	public int[] get(byte[] key, int length) {
		int match = dictionary.get(new Key(key, length));
		if (match == -2) {
			return new int[0];
		}
		int[] result = new int[4];
		result[0] = length > 1 ? dictionary.get(new Key(key, length - 1)) : match;
		result[1] = length > 2 ? dictionary.get(new Key(key, length - 2)) : match;
		result[2] = length > 3 ? dictionary.get(new Key(key, length - 3)) : match;
		result[3] = match;
		return result;
	}

	/**
	 * Store 'value' at the given byte[] key, only considering the first 'length' elements of the key. After calling, 
	 * callers must not modify any elements of the key within this range.
	 */
	public void put(byte[] key, int length, int value) {
		dictionary.put(new Key(key, length), value);
		
	}



}
