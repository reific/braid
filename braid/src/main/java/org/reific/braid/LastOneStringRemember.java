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

import java.util.Objects;

class LastOneStringRemember implements Rememberer {

	private String lastKey;
	private Braid lastValue;

	@Override
	public void maybeRemember(String key, Braid value) {
		lastKey = key;
		lastValue = value;
	}

	@Override
	public Braid maybeRecall(String key) {
		if (Objects.equals(key, lastKey)) {
			return lastValue;
		}
		return null;
	}

}
