package org.reific.braid;

public class BraidImpl implements Braid {
	
	private final InternalKnot knot;
	/**
	 * 
	 */
	private int index;
	
	BraidImpl(InternalKnot knot, CharSequence string) {
		this.knot = knot;
		this.index = this.knot.addString(string);
	}

	public String get(){
		return knot.lookupString(index);
	}

}
