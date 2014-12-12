package org.reific.braid;

interface MutableInternalKnot {
	/**
	 * Represents the result of setting the String value of the MutableBraid
	 */
	static class Result {
		final int index;
		final MutableInternalKnot knot;

		Result(int index, MutableInternalKnot knot) {
			this.knot = knot;
			this.index = index;
		}
	}

	Result addString(String string);
	String lookupString(int index);
}
