package org.reific.braid;

interface MutableInternalKnot {
	static class Result {
		public final int index;
		public final MutableInternalKnot knot;

		public Result(int index, MutableInternalKnot knot) {

			this.knot = knot;
			this.index = index;
		}
	}

	public Result addString(String string);
	public String lookupString(int index);
}
