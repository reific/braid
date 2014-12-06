package org.reific.braid;

import java.util.List;

class KnotImpl implements Knot, InternalKnot {

	private final KnotStorage knotStorage;
	private final Interners interners;
	private final Rememberer rememberers;
	private final MutableBraidKnotWrapper mutableBraidKnotWrapper;
	
	KnotImpl(Interners interners, KnotStorage knotStorage, Rememberer rememberer,
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
	public int getCompressedSize() {
		return knotStorage.getCompressedSize();
	}

	private static final Braid NULL_BRAID = new Braid() {

		@Override
		public String get() {
			return null;
		}
	};

	@Override
	public Braid braid(String string) {
		if (string == null) {
			return NULL_BRAID;
		}
		Braid possibleInternMatch = interners.attemptToIntern(string);
		if (possibleInternMatch != null) {
			return possibleInternMatch;
		}
		Braid possibleRecall = rememberers.maybeRecall(string);
		if (possibleRecall != null) {
			return possibleRecall;
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
