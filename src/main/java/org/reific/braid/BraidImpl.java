package org.reific.braid;

public class BraidImpl implements Braid {
	
	private final InternalKnot knot;
	private int index;
	
	BraidImpl(Knot knot, CharSequence string) {
		this.knot = (InternalKnot) knot;
		this.index = this.knot.addString(string);
	}

	public String get(){
		return knot.lookupString(index);
	}

}
