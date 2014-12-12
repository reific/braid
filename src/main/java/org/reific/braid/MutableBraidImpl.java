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

import org.reific.braid.MutableInternalKnot.Result;

final class MutableBraidImpl implements MutableBraid {


	private static final int SET_TO_NULL = -2;
	private static final int HASHCODE_UNINITIALIZED = 1;

	private MutableInternalKnot knot;
	private int index;
	// Cached hashcode of the String. We use 1 to indicate uninitialized instead of 0 (which String uses), as 0 (which is the 
	// hash of the empty String) is more common than other values.
	private int hash = HASHCODE_UNINITIALIZED;

	MutableBraidImpl(MutableInternalKnot knot, String value) {
		if (knot == null) {
			throw new NullPointerException("null Knot");
		}
		this.knot = knot;
		set(value);
	}

	@Override
	public String get() {
		if (index == SET_TO_NULL) {
			return null;
		}
		return knot.lookupString(index);
	}

	@Override
	public void set(String value) {
		this.hash = HASHCODE_UNINITIALIZED;
		if (value == null) {
			this.index = SET_TO_NULL;
		} else {
			Result result = this.knot.addString(value);
			this.index = result.index;
			this.knot = result.knot;
		}
	}

	@Override
	public int hashCode() {
		if (index == SET_TO_NULL) {
			return SET_TO_NULL_HASH;
		}
		if (hash == 1) {
			hash = get().hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object anObject) {
		//TODO: if other instance is a MutableBraidImpl, and knots are the same, just check the indexes, or maybe delegate this to the braid?
		return BraidUtil.equals(this, anObject);
	}

}
