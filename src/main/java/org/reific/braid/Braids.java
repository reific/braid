package org.reific.braid;

public class Braids {

	private static Braid NULL_BRAID = new Braid() {

		@Override
		public String get() {
			return null;
		}
	};
	public static Braid newBraid(Knot knot, CharSequence string){
		if (string == null) {
			return NULL_BRAID;
		}
		return new BraidImpl((InternalKnot)knot, string);
	}
}
