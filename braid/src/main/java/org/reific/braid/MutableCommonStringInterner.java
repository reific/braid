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

import java.util.Set;

class MutableCommonStringInterner implements MutableInterner {

	private final TObjectIntMap<String> commonStringsLookup;
	private final String[] commonStrings;
	private static final int NO_ENTRY_VALUE = -1;

	public MutableCommonStringInterner(Set<String> commonStrings) {
		this.commonStrings = new String[commonStrings.size()];
		this.commonStringsLookup = new TObjectIntHashMap<String>(commonStrings.size(), 1, NO_ENTRY_VALUE);
		int i = 0;
		for (String string : commonStrings) {
			commonStringsLookup.put(string, i);
			this.commonStrings[i++] = string;
		}

	}

	@Override
	public int attemptToIntern(String value) {
		int i = commonStringsLookup.get(value);
		return i;
	}

	@Override
	public String getInternedValue(int index) {
		return commonStrings[index];
	}

}
