package org.reific.braid;

import org.reific.braid.knots.ExampleKnotStorageImpl;
import org.reific.braid.knots.LZ78KnotStorage;

public class Knots {

	public static Knot example() {
		return new KnotImpl(new ExampleKnotStorageImpl());
	}

	public static Knot lz78() {
		return new KnotImpl(new LZ78KnotStorage());
	}

}
