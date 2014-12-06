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

import java.util.ArrayList;
import java.util.List;

class MutableBraidKnotWrapper implements MutableInternalKnot {

	private final List<MutableInternerWrapper> mutableInternerWrappers;
	private final KnotImpl knotImpl;

	MutableBraidKnotWrapper(KnotImpl knotImpl, List<MutableInterner> mutableInterners) {
		this.knotImpl = knotImpl;
		mutableInternerWrappers = new ArrayList<MutableInternerWrapper>(mutableInterners.size());

		for (MutableInterner mutableInterner : mutableInterners) {
			mutableInternerWrappers.add(new MutableInternerWrapper(this, mutableInterner));
		}
	}

	@Override
	public Result addString(String string) {
		for (MutableInternerWrapper mutableInternerWrapper : mutableInternerWrappers) {
			int attempToIntern = mutableInternerWrapper.attempToIntern(string);
			if (attempToIntern >= 0) {
				//TODO Object churn?
				return new Result(attempToIntern, mutableInternerWrapper);
			}
		}
		// TODO call to remembers.maybeRecall(string)
		int index = knotImpl.addString(string);
		// TODO call to remembers.maybeRemember(string, index)
		//TODO Object churn?
		return new Result(index, this);
	}

	@Override
	public String lookupString(int index) {
		return knotImpl.lookupString(index);
	}

}
