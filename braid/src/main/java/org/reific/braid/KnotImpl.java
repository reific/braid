package org.reific.braid;

import java.util.List;

class KnotImpl implements Knot, InternalKnot {

	private final KnotStorage knotStorage;
	private final Interner interners;
	private final Rememberer rememberers;
	private final MutableBraidKnotWrapper mutableBraidKnotWrapper;
	
	KnotImpl(Interner interners, KnotStorage knotStorage, Rememberer rememberer,
 List<MutableInterner> mutableInterners) {
		this.interners = interners;
		this.knotStorage = knotStorage;
		this.rememberers = rememberer;

		mutableBraidKnotWrapper = new MutableBraidKnotWrapper(this, mutableInterners);
	}
	@Override
	public int addString(String string) {
		return knotStorage.store(string);
	}
	@Override
	public String lookupString(int index) {
		return knotStorage.lookup(index);
	}
	@Override
	public void flush() {
		knotStorage.flush();
	}

	@Override
	public long spaceUsed() {
		return knotStorage.spaceUsed();
	}

	private static final Braid NULL_BRAID = new Braid() {

		@Override
		public String get() {
			return null;
		}

		@Override
		public int hashCode() {
			return SET_TO_NULL_HASH;
		}

		@Override
		public boolean equals(Object obj) {
			return BraidUtil.equals(this, obj);
		}
	};
	private static final Braid EMPTY_STRING_BRAID = new Braid() {

		@Override
		public String get() {
			return "";
		}

		@Override
		public int hashCode() {
			return "".hashCode();
		};

		@Override
		public boolean equals(Object obj) {
			return BraidUtil.equals(this, obj);
		}
	};

	@Override
	public Braid braid(String string) {
		if (string == null) {
			return NULL_BRAID;
		}
		if (string.equals("")) {
			return EMPTY_STRING_BRAID;
		}
		// Check Rememberers first, since they are a bit faster that Interners
		// (Probably due to temporal cache locality)
		Braid possibleRecall = rememberers.maybeRecall(string);
		if (possibleRecall != null) {
			return possibleRecall;
		}
		Braid possibleInternMatch = interners.attemptToIntern(string);
		if (possibleInternMatch != null) {
			// TODO: consider remembering this match
			return possibleInternMatch;
		}
		BraidImpl newBraid = new BraidImpl(this, string);
		rememberers.maybeRemember(string, newBraid);

		return newBraid;
	}

	@Override
	public MutableBraid mutableBraid(String string) {
		return new MutableBraidImpl(mutableBraidKnotWrapper, string);
	}

}
