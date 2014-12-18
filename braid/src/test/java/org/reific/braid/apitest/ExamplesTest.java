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

import org.reific.braid.Braid;
import org.reific.braid.Knot;
import org.reific.braid.Knots;

public class ExamplesTest {

	// Example of a primary use case of Braid
	static class Book {

		//
		private static Knot authorKnot = Knots.builder().common("Anonymous").build();
		private static Knot publisherKnot = Knots.builder().build();
		private static Knot titleKnot = Knots.builder().direct().build();
		// In a Pojo, instead of storing String attributes, a Braid is used,
		// which is capable of storing the string data compressed.
		private final Braid author;
		private final Braid publisher;
		private final Braid title;

		public Book(String author, String publisher, String title) {
			// Any code that sets the string value of the Braid needs to provide
			// a Knot, which is where all the related Braids are compressed.
			this.author = authorKnot.braid(author);
			this.publisher = publisherKnot.braid(publisher);
			this.title = titleKnot.braid(title);
		}

		public String getAuthor() {
			// Getting the String data from the Braid is easy. There is a small
			// computational overhead, as the requested String will need to be
			// decompressed.
			return author.get();
		}

		public String getPublisher() {
			return publisher.get();
		}

		public String getTitle() {
			return title.get();
		}
	}

}
