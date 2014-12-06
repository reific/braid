package org.reific.braid.knots.lz78;

import static org.hamcrest.Matchers.theInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.reific.braid.Braid;
import org.reific.braid.Knot;
import org.reific.braid.Knots;

public class Lz78Test {

	@Test
	public void testBasicFunctionality() {
		final Knot knot = Knots.builder().build();
		String string = new String("this that the other");
		Braid braid = knot.braid(string);
		assertEquals(string, braid.get());
		// A new String instance has been created from the compressed data.
		assertNotSame(string, braid.get());
	}

	@Test
	public void testInternCommonStrings() throws Exception {
		String hello = new String("Hello");
		final Knot knot = Knots.builder().common(hello).build();
		assertThat(knot.braid(new String(hello)).get(), theInstance(hello));
	}

	@Test
	public void testMultibyteUnicode() throws Exception {
		final Knot knot = Knots.builder().build();

		// $ Single byte in UTF-8
		String string = new String("\u0024");
		Braid braid = knot.braid(string);
		assertEquals("$", braid.get());
		// ¢ (cent) 2-byte in UTF-8 
		string = new String("\u00A2");
		braid = knot.braid(string);
		assertEquals("\u00A2", braid.get());

		//€ (euro) 3-byte in UTF-8
		string = new String("\u20AC");
		braid = knot.braid(string);
		assertEquals("\u20AC", braid.get());

		// 4 byte in UTF-8
		string = new String("\u24B62");
		braid = knot.braid(string);
		assertEquals("\u24B62", braid.get());
	}

	@Test
	public void testNullString() throws Exception {
		final Knot knot = Knots.builder().build();
		Braid braid = knot.braid(null);
		assertNull(braid.get());
	}

	@Test
	public void testBasicEdgeCases() {

		final Knot knot = Knots.builder().build();

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
			Braid braid = knot.braid(new String(s));
			assertEquals(s, braid.get());
		}
	}

	@Test
	/**
	 * Parse and Braid-compress a large amount of text, as an attempt to catch any edge cases that might have been overlooked
	 */
	public void testTextFile() throws Exception {

		final Knot knot = Knots.builder().build();
		List<String> uncompressed = new ArrayList<String>();
		List<Braid> compressed = new ArrayList<Braid>();

		InputStream stream = this.getClass().getResourceAsStream("/hayek-road-to-serfdom.txt");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			for (String line; (line = br.readLine()) != null;) {
				uncompressed.add(line);
				compressed.add(knot.braid(line));
			}
		}
		assertEquals(uncompressed.size(), compressed.size());
		for (int i = 0; i < uncompressed.size(); i++) {
			assertEquals(uncompressed.get(i), compressed.get(i).get());
		}
	}

}
