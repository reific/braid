package org.reific.braid;

public class KnotImpl implements Knot, InternalKnot {

	private final KnotStorage knotStorage;
	
	public KnotImpl(KnotStorage knotStorage) {
		this.knotStorage = knotStorage;
		
	}
	@Override
	public int addString(CharSequence string) {
		return knotStorage.store(string.toString());
	}
	@Override
	public String lookupString(int index) {
		return knotStorage.lookup(index);
	}

}
