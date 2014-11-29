package org.reific.braid.knots.lz78;

import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
		// minimum size
		assertEquals(128, knot.getCompressedSize());
	}

	@Test
	public void testMultibyteUnicode() throws Exception {
		final Knot knot = Knots.lz78();

		// $ Single byte in UTF-8
		String string = new String("\u0024");
		Braid braid = Braids.newBraid(knot, string);
		assertEquals("$", braid.get());
		// ¢ (cent) 2-byte in UTF-8 
		string = new String("\u00A2");
		braid = Braids.newBraid(knot, string);
		assertEquals("\u00A2", braid.get());

		//€ (euro) 3-byte in UTF-8
		string = new String("\u20AC");
		braid = Braids.newBraid(knot, string);
		assertEquals("\u20AC", braid.get());

		// 4 byte in UTF-8
		string = new String("\u24B62");
		braid = Braids.newBraid(knot, string);
		assertEquals("\u24B62", braid.get());
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
	/**
	 * 
	 */
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

	@Test
	public void testCompressionRatio() throws Exception {

		final Knot knot = Knots.lz78();
		int uncompressedSizeUtf8 = 0;
		int uncompressedSizeUtf16 = 0;

		for (int i = 0; i < 10000; i++) {
			String line = "Science is the great antidote to the poison of enthusiasm and superstition.";

			uncompressedSizeUtf8 += line.getBytes(StandardCharsets.UTF_8).length;
			uncompressedSizeUtf16 += line.getBytes(StandardCharsets.UTF_16).length;
			Braids.newBraid(knot, line);
		}

		System.out.println("testCompressionRatio Compressed size (% of utf8 string):" + knot.getCompressedSize()
				/ (double) uncompressedSizeUtf8);
		System.out.println("testCompressionRatio Compressed size (% of utf16 string):" + knot.getCompressedSize()
				/ (double) uncompressedSizeUtf16);

		assertThat((double) knot.getCompressedSize(), lessThan(uncompressedSizeUtf8 * 0.08));
		assertThat((double) knot.getCompressedSize(), lessThan(uncompressedSizeUtf16 * 0.04));
	}

	@Test
	public void testCompressionRatioVerySmallStrings() throws Exception {

		final Knot knot = Knots.lz78();
		int uncompressedSizeUtf8 = 0;
		int uncompressedSizeUtf16 = 0;

		for (int i = 0; i < 10000; i++) {
			String line = "Hi.";

			uncompressedSizeUtf8 += line.getBytes(StandardCharsets.UTF_8).length;
			uncompressedSizeUtf16 += line.getBytes(StandardCharsets.UTF_16).length;
			Braids.newBraid(knot, line);
		}

		System.out.println("testCompressionRatioVerySmallStrings Compressed size (% of utf8 string):"
				+ knot.getCompressedSize()
				/ (double) uncompressedSizeUtf8);
		System.out.println("testCompressionRatioVerySmallStrings Compressed size (% of utf16 string):"
				+ knot.getCompressedSize()
				/ (double) uncompressedSizeUtf16);

		//TODO: fix small string compression.
		// LZ78KnotStorage currently uses a minimum of 3 bytes for any string (size+lastCharacter+pointer).
		//assertThat((double) knot.getCompressedSize(), lessThan(uncompressedSizeUtf8 * 0.08));
		//assertThat((double) knot.getCompressedSize(), lessThan(uncompressedSizeUtf16 * 0.04));
	}

	@Test
	/**
	 * 
	 */
	public void testCompressionRatioReadingFile() throws Exception {

		final Knot knot = Knots.lz78();
		int uncompressedSizeUtf8 = 0;
		int uncompressedSizeUtf16 = 0;

		InputStream stream = this.getClass().getResourceAsStream("/hayek-road-to-serfdom-100-lines.txt");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			for (String line; (line = br.readLine()) != null;) {
				//loop same data multiple times to test highly compressible data
				for (int j = 0; j < 100; j++) {
					uncompressedSizeUtf8 += line.getBytes(StandardCharsets.UTF_8).length;
					uncompressedSizeUtf16 += line.getBytes(StandardCharsets.UTF_16).length;
					Braids.newBraid(knot, line);
				}
			}
		}

		System.out.println("Compressed size (% of utf8 string):" + knot.getCompressedSize()
				/ (double) uncompressedSizeUtf8);
		System.out.println("Compressed size (% of utf16 string):" + knot.getCompressedSize()
				/ (double) uncompressedSizeUtf16);

		assertThat((double) knot.getCompressedSize(), lessThan(uncompressedSizeUtf8 * 0.3));
		assertThat((double) knot.getCompressedSize(), lessThan(uncompressedSizeUtf16 * 0.15));
	}

}
