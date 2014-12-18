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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MutableBraidTest {


	@Test
	public void basicTest() throws Exception {

		final String s1 = "There are a thousand hacking at the branches of evil to one who is striking at the root.";
		final String s2 = "Every generation laughs at the old fashions, but follows religiously the new.";

		Knot knot = Knots.builder().build();
		MutableBraid braid = knot.mutableBraid(s1);
		assertThat(braid.get(), equalTo(s1));
		braid.set(s2);
		assertThat(braid.get(), equalTo(s2));
	}

	@Test
	public void testNull() throws Exception {

		Knot knot = Knots.builder().build();
		MutableBraid braid = knot.mutableBraid(null);
		assertThat(braid.get(), equalTo(null));
		braid.set("");
		assertThat(braid.get(), equalTo(""));
		braid.set(null);
		assertThat(braid.get(), equalTo(null));
	}

	@Test
	public void testCommonStrings() throws Exception {
		String commonString = "Hello";
		String commonString2 = "Common String 2";
		Knot knot = Knots.builder().common(commonString, commonString2).build();
		MutableBraid braid = knot.mutableBraid(commonString);
		assertThat(braid.get(), sameInstance(commonString));
		String s2 = "String2";
		braid.set(s2);
		assertThat(braid.get(), equalTo(s2));
		assertThat(braid.get(), not(sameInstance(s2)));

		String nullString = null;
		braid.set(nullString);
		assertThat(braid.get(), equalTo(nullString));

		braid.set(commonString);
		assertThat(braid.get(), sameInstance(commonString));

		braid.set(commonString2);
		assertThat(braid.get(), sameInstance(commonString2));

	}

}
