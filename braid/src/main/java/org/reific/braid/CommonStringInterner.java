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

import java.util.Set;

final class CommonStringInterner implements Interner {

	private final String[] strings;
	private final Braid[] braids;

	CommonStringInterner(Set<String> commonStrings) {
		this.strings = new String[commonStrings.size()];
		this.braids = new Braid[commonStrings.size()];
		int i = 0;
		for (String string : commonStrings) {
			this.strings[i] = string;
			this.braids[i] = new InternedBraid(string);
			i++;
		}
	}

	@Override
	public Braid attemptToIntern(final String value) {
		for (int i = 0; i < strings.length; i++) {
			if (strings[i].equals(value)) {
				return braids[i];
			}
		}
		return null;

	}

}
