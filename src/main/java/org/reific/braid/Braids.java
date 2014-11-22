package org.reific.braid;

public class Braids {

	public static Braid newExampleBraid(Knot knot, CharSequence string){
		return new BraidImpl((InternalKnot)knot, string);
	}
}
