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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LZ78HashOnlyDictionaryTest {
	@Test
	public void testName() throws Exception {
		LZ78HashOnlyDictionary dictionary = new LZ78HashOnlyDictionary(2, 0.99f);
		byte[] array = new byte[] { 1, 2, 3 };
		dictionary.put(array, 0, 1, 100);
		int index = dictionary.indexOfLongestPossiblePrefix(array, 0, 1);
		assertThat(index, equalTo(0));

		dictionary.put(array, 0, 2, 100);
		index = dictionary.indexOfLongestPossiblePrefix(array, 0, 2);
		assertThat(index, equalTo(1));

		dictionary.put(array, 0, 3, 100);
		index = dictionary.indexOfLongestPossiblePrefix(array, 0, 3);
		assertThat(index, equalTo(2));

	}

}
