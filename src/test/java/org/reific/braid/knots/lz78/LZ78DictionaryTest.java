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
package org.reific.braid.knots.lz78;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LZ78DictionaryTest {

	@Test
	/**
	 * ensure lookup is only done on the array within the length specified
	 */
	public void testSubArryMatch() throws Exception {

		byte[] a = new byte[] { 32, 109, };
		byte[] b = new byte[] { 32, 111, };

		LZ78Dictionary dict = new LZ78Dictionary(128, 0.5f, -2);
		dict.put(a, 1, 99);
		assertThat(dict.get(a, 1), equalTo(99));
		assertThat(dict.get(b, 1), equalTo(99));
	}

	@Test
	public void testEmptyKey() throws Exception {

		byte[] a = new byte[] {};

		LZ78Dictionary dict = new LZ78Dictionary(128, 0.5f, -2);
		dict.put(a, 0, 99);
		assertThat(dict.get(a, 0), equalTo(99));

		byte[] b = new byte[] { 32, };

		dict.put(b, 0, 66);
		assertThat(dict.get(b, 0), equalTo(66));
	}


}
