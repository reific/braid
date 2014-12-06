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

class MutableBraidImpl implements MutableBraid {

	private static final int NOT_SET = -1;
	private static final int SET_TO_NULL = -2;

	private MutableInternalKnot knot;
	private int index;

	MutableBraidImpl(MutableInternalKnot knot, String value) {
		this.knot = knot;
		set(value);
	}

	@Override
	public String get() {
		if (index == SET_TO_NULL) {
			return null;
		}
		if (index == NOT_SET) {
			throw new RuntimeException("Braid value not set");
		}
		return knot.lookupString(index);
	}

	@Override
	public void set(String value) {
		if (value == null) {
			this.index = SET_TO_NULL;
		} else {
			Result result = this.knot.addString(value);
			this.index = result.index;
			this.knot = result.knot;
		}
	}

}
