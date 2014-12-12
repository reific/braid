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
package org.reific.braid.apitest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reific.braid.Braid;
import org.reific.braid.Knot;
import org.reific.braid.Knots;
import org.reific.braid.MutableBraid;

@RunWith(Parameterized.class)
public class EqualsHashcodeTest {

	// Test Parameters
	private Knot knot;
	private Knot knot2;

	@Parameters
	public static Collection<Object[]> data() {

		// Run each of the tests with various Knots
		List<Supplier<Knot>> knots = Arrays.asList(
				() -> Knots.builder().build(),
				() -> Knots.builder().direct().build(),
				() -> Knots.builder().rememberLast(1).build(),
				() -> Knots.builder().common("a").build(),
				() -> Knots.builder().common("a", "Hello").build(),
				() -> Knots.builder().common("", "Hello").build(),
				() -> Knots.builder().lz78(8).build()
				);

		Collection<Object[]> data = new ArrayList<>();

		// All permutations of knots. If the same Supplier is provided for both knots, a single Knot will be used.
		for (Supplier<Knot> s1 : knots) {
			for (Supplier<Knot> s2 : knots) {
				data.add(new Object[] { s1, s2 });
			}
		}
		return data;
    }

	// Create the parameters for each test case. New knots will be constructed for each test.
	public EqualsHashcodeTest(Supplier<Knot> knotSupplier, Supplier<Knot> knotSupplier2) {
		this.knot = knotSupplier.get();
		// If the same Supplier is provided for both knots, a single Knot will be used.
		// (Rather than creating two identical instances)
		if (knotSupplier == knotSupplier2) {
			this.knot2 = this.knot;
		} else {
			this.knot2 = knotSupplier2.get();
		}

	}

	@Test
	public void testNull() throws Exception {
		assertThat(knot.braid(null), not(equalTo(null)));
	}

	@Test
	public void testEqualsBraids() throws Exception {
		assertThat(knot.braid(null), fullyEqual(knot2.braid(null)));
		assertThat(knot.braid(""), fullyEqual(knot2.braid("")));
		assertThat(knot.braid("a"), fullyEqual(knot2.braid("a")));
	}

	@Test
	public void testEqualsBraidsMultipleKnots() throws Exception {
		assertThat(knot.braid(null), fullyEqual(knot2.braid(null)));
		assertThat(knot.braid(""), fullyEqual(knot2.braid("")));
		assertThat(knot.braid("a"), fullyEqual(knot2.braid("a")));
	}

	@Test
	public void testEqualsMutableBraidsMultipleKnots() throws Exception {
		assertThat(knot.braid(null), fullyEqual(knot2.braid(null)));
		assertThat(knot.braid(""), fullyEqual(knot2.braid("")));
		assertThat(knot.braid("a"), fullyEqual(knot2.braid("a")));
	}

	@Test
	public void testEqualsBraidsAndMutableBraids() throws Exception {
		assertThat(knot.mutableBraid(null), fullyEqual(knot2.mutableBraid(null)));
		assertThat(knot.mutableBraid(""), fullyEqual(knot2.mutableBraid("")));
		assertThat(knot.mutableBraid("a"), fullyEqual(knot2.mutableBraid("a")));

		Braid b1 = knot.braid(null);
		Braid b2 = knot.braid("");
		Braid b3 = knot.braid("Hello World");

		MutableBraid mb1 = knot2.mutableBraid(null);
		MutableBraid mb2 = knot2.mutableBraid("");
		MutableBraid mb3 = knot2.mutableBraid("Hello World");

		assertThat(b1, fullyEqual(mb1));
		assertThat(b2, fullyEqual(mb2));
		assertThat(b3, fullyEqual(mb3));

		mb1.set("a");
		mb2.set("b");
		mb3.set("c");

		assertThat(b1, not(fullyEqual(mb1)));
		assertThat(b2, not(fullyEqual(mb2)));
		assertThat(b3, not(fullyEqual(mb3)));

		mb1.set(null);
		mb2.set("");
		mb3.set("Hello World");

		assertThat(b1, fullyEqual(mb1));
		assertThat(b2, fullyEqual(mb2));
		assertThat(b3, fullyEqual(mb3));

	}

	@Test
	public void testEqualsMutableBraids() throws Exception {
		MutableBraid b1 = knot.mutableBraid(null);
		MutableBraid b2 = knot.mutableBraid("");
		MutableBraid b3 = knot2.mutableBraid("Hello World");

		assertThat(b1, not(fullyEqual(b2)));
		assertThat(b1, not(fullyEqual(b3)));
		assertThat(b2, not(fullyEqual(b3)));

		b2.set(null);
		b3.set(null);

		assertThat(b1, fullyEqual(b1));
		assertThat(b1, fullyEqual(b2));
		assertThat(b1, fullyEqual(b3));
		assertThat(b2, fullyEqual(b2));
		assertThat(b2, fullyEqual(b3));
		assertThat(b3, fullyEqual(b3));

		b1.set("");
		b2.set("");
		b3.set("");

		assertThat(b1, fullyEqual(b1));
		assertThat(b1, fullyEqual(b2));
		assertThat(b1, fullyEqual(b3));
		assertThat(b2, fullyEqual(b2));
		assertThat(b2, fullyEqual(b3));
		assertThat(b3, fullyEqual(b3));

		b1.set("Hello");
		b2.set("Hello");
		b3.set("Hello");

		assertThat(b1, fullyEqual(b1));
		assertThat(b1, fullyEqual(b2));
		assertThat(b1, fullyEqual(b3));
		assertThat(b2, fullyEqual(b2));
		assertThat(b2, fullyEqual(b3));
		assertThat(b3, fullyEqual(b3));

		b1.set(null);
		b2.set("");
		b3.set("Some Other Value");

		assertThat(b1, fullyEqual(b1));
		assertThat(b2, fullyEqual(b2));
		assertThat(b3, fullyEqual(b3));

		assertThat(b1, not(fullyEqual(b2)));
		assertThat(b1, not(fullyEqual(b3)));
		assertThat(b2, not(fullyEqual(b3)));
		assertThat(b3, not(fullyEqual(b2)));

		b1.set("x");
		b2.set("y");
		b3.set("z");

		assertThat(b1, fullyEqual(b1));
		assertThat(b2, fullyEqual(b2));
		assertThat(b3, fullyEqual(b3));

		assertThat(b1, not(fullyEqual(b2)));
		assertThat(b1, not(fullyEqual(b3)));
		assertThat(b2, not(fullyEqual(b3)));
	}

	// Hamcrest matcher that ensures the equals/hashcode contract is fully satisfied
	private Matcher<Object> fullyEqual(final Object other) {
		return new TypeSafeMatcher<Object>() {

			String problem;

			@Override
			public boolean matchesSafely(final Object braid) {

				for (int i = 0; i < 10; i++) {
					if (braid.hashCode() != braid.hashCode()) {
						problem = "hashcode() not returning consistent results on first object";
						return false;
					}
				}
				for (int i = 0; i < 10; i++) {
					if (other.hashCode() != other.hashCode()) {
						problem = "hashcode() not returning consistent results on second object";
						return false;
					}
				}
				if (braid.hashCode() != other.hashCode()) {
					problem = "hashcode()'s not equal";
					return false;
				}
				if (!braid.equals(braid)) {
					problem = "first object not equal to itself";
					return false;
				}
				if (!other.equals(other)) {
					problem = "second object not equal to itself";
					return false;
				}
				if (!braid.equals(other)) {
					problem = "not equal";
					return false;
				}
				if (!other.equals(braid)) {
					problem = "not symetrically equal";
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("Was expecting two equal Objects");
			}

			@Override
			protected void describeMismatchSafely(final Object braid, final Description mismatchDescription) {
				mismatchDescription.appendText(problem);

			}
		};
	}
}
