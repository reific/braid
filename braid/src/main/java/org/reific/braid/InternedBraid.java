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


final class InternedBraid implements Braid {

	private final String string;

	InternedBraid(String string) {
		this.string = string;
	}
	@Override
	public String get() {
		return string;
	}

	@Override
	public int hashCode() {
		if (string == null) {
			return SET_TO_NULL_HASH;
		}
		return string.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return BraidUtil.equals(this, obj);
	}

}
