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

final class BraidUtil {
	static boolean equals(Braid braid, Object otherPossibleBraid) {
		if (braid == otherPossibleBraid) {
			return true;
		}
		if (braid == null || otherPossibleBraid == null) {
			return false;
		}
		if (otherPossibleBraid instanceof Braid) {
			Braid anotherBraid = (Braid) otherPossibleBraid;
			if (braid.hashCode() != anotherBraid.hashCode()) {
				return false;
			}
			// Only if the hashcodes are equal do we de-compress the braids and compare the strings.
			return Objects.equals(braid.get(), anotherBraid.get());
		}
		return false;
	}

}
