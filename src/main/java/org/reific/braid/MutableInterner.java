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

/**
 * An interner (in the sense of {@link String#intern()}) for {@link MutableBraid}s. Serves the same purpose as {@link Interner} does for Braids. Since a {@link MutableBraid} change be change
 * at any time, a {@link MutableInterner} can't simply return a constant Braid as {@link Interner} can since this would prevent 
 * the Braid from being updated in the future. 
 */
interface MutableInterner {
	/**
	 * Gives this {@link MutableInterner} a chance to 'intern' (in the sense of {@link String#intern()}) the provided String value.
	 * If this {@link MutableInterner} chooses to intern the String, then a non-negative int result will be returned, which represents a memento
	 * that can later be used to lookup the String value. The returned int should be considered opaque, and has no meaning outside the
	 * context of this {@link MutableInterner} (other than the negative/non-negative distinction)
	 * 
	 * @return a non-negative int memento for the provided String value if this {@link MutableInterner} chooses to intern the value, negative otherwise.
	 */
	int attemptToIntern(String value);
	String getInternedValue(int index);
}
