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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Ignore;
import org.junit.Test;

public class CompressionRatioTest {

	@Test
	public void testCompressionRatio() throws Exception {

		final Knot knot = Knots.builder().build();
		int uncompressedSizeUtf16 = 0;

		for (int i = 0; i < 10000; i++) {
			String line = "Science is the great antidote to the poison of enthusiasm and superstition.";

			uncompressedSizeUtf16 += line.getBytes(StandardCharsets.UTF_16).length;
			knot.braid(line);
		}

		assertThat((double) knot.getCompressedSize() / uncompressedSizeUtf16, closeTo(0.032605, 0.000001));
	}

	@Test
	public void testCompressionRatioRememberLast() throws Exception {

		// This string currently compresses to 91 bytes
		String line = "Science is the great antidote to the poison of enthusiasm and superstition.";
		final int BUFFER_SIZE = 91;
		final Knot knot = Knots.builder()
		// remember the last string
				.rememberLast(1)
				// explicit size and ratio (for comparing)
				.lz78(BUFFER_SIZE, 1.0f).build();

		assertThat(knot.getCompressedSize(), equalTo(BUFFER_SIZE));
		knot.braid(new String(line));
		assertThat(knot.getCompressedSize(), equalTo(BUFFER_SIZE));

		//Ensure that adding the same string many times does not change the buffer size
		for (int i = 0; i < 100; i++) {

			knot.braid(new String(line));
		}
		assertThat(knot.getCompressedSize(), equalTo(BUFFER_SIZE));
		// now add some more unrelated data.
		knot.braid(new String("S"));
		// The buffer should double, based on the growth factor
		assertThat(knot.getCompressedSize(), equalTo(2 * BUFFER_SIZE));

	}

	@Test
	@Ignore("Fix small string compression. LZ78KnotStorage currently uses a minimum of 3 bytes for any string (size+lastCharacter+pointer")
	public void testCompressionRatioVerySmallStrings() throws Exception {

		final Knot knot = Knots.builder().build();
		int uncompressedSizeUtf16 = 0;

		for (int i = 0; i < 10000; i++) {
			String line = "Hi.";

			uncompressedSizeUtf16 += line.getBytes(StandardCharsets.UTF_16).length;
			knot.braid(line);
		}

		// No idea what the correct compression should once this test is fixed
		assertThat((double) knot.getCompressedSize() / uncompressedSizeUtf16, closeTo(0.131846, 0.000001));
	}

	@Test
	public void testCompressionRatioReadingFile() throws Exception {

		final Knot knot = Knots.builder().build();
		int uncompressedSizeUtf16 = 0;

		InputStream stream = this.getClass().getResourceAsStream("/hayek-road-to-serfdom-100-lines.txt");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			for (String line; (line = br.readLine()) != null;) {
				//loop same data multiple times to test highly compressible data
				for (int j = 0; j < 100; j++) {
					uncompressedSizeUtf16 += line.getBytes(StandardCharsets.UTF_16).length;
					knot.braid(line);
				}
			}
		}

		// Make explicit assertions about compression ratios, so that if anything chages (good or bad),
		// we know it.
		assertThat((double) knot.getCompressedSize() / uncompressedSizeUtf16, closeTo(0.131846, 0.000001));
	}
}
