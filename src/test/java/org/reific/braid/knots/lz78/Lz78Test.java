package org.reific.braid.knots.lz78;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.reific.braid.Braid;
import org.reific.braid.Braids;
import org.reific.braid.Knot;
import org.reific.braid.Knots;

public class Lz78Test {

	// Example of a primary use case of Braid
	class ExamplePojo {

		// In a Pojo, instead of storing String attributes, a Braid is used,
		// which is capable of storing the string data compressed.
		private Braid value;

		public ExamplePojo(Knot knot, String string) {
			// Any code that sets the string value of the Braid needs to provide
			// a Knot, which is where all the related Braids are compressed.
			this.value = Braids.newBraid(knot, string);
		}

		public String getValue() {
			// Getting the String data from the Braid is easy. There is a small
			// computational overhead, as the requested String will need to be
			// decompressed.
			return value.get();
		}
	}

	@Test
	public void testBasicFunctionality() {
		final Knot knot = Knots.lz78();
		String string = new String("this that the other");
		Braid braid = Braids.newBraid(knot, string);
		assertEquals(string, braid.get());
		assertNotSame(string, braid.get());
	}

	@Test
	public void testNullString() throws Exception {
		final Knot knot = Knots.lz78();
		Braid braid = Braids.newBraid(knot, null);
		assertNull(braid.get());
	}

	@Test
	public void testBasicEdgeCases() {

		final Knot knot = Knots.lz78();

		List<String> list = new ArrayList<String>();
		list.add("");
		list.add(" ");
		list.add("");
		list.add("a");
		list.add("aa");
		list.add("aaa");
		list.add("aaa");
		list.add("ab");
		list.add("aba");
		list.add("abab");
		list.add("abababaa");

		for (String s : list) {
			Braid braid = Braids.newBraid(knot, new String(s));
			assertEquals(s, braid.get());
		}
	}

	@Test
	public void testTextFile() throws Exception {

		final Knot knot = Knots.lz78();
		List<String> uncompressed = new ArrayList<String>();
		List<Braid> compressed = new ArrayList<Braid>();

		InputStream stream = this.getClass().getResourceAsStream("/hayek-road-to-serfdom.txt");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			for (String line; (line = br.readLine()) != null;) {
				uncompressed.add(line);
				compressed.add(Braids.newBraid(knot, line));
			}
		}
		assertEquals(uncompressed.size(), compressed.size());
		for (int i = 0; i < uncompressed.size(); i++) {
			assertEquals(uncompressed.get(i), compressed.get(i).get());
		}
	}

}
