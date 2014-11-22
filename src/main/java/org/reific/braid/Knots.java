package org.reific.braid;

public class Knots {

	public static Knot example() {
		return new KnotImpl(new ExampleKnotStorageImpl());
	}

}
