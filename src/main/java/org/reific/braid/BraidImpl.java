package org.reific.braid;

/**
 * A note on memory usage. 
 * 
 * Java objects are aligned to 8 byte boundaries, and are padded as required.
 * 
 * As measured with java.lang.instrument.Instrumentation using:
 * 
 *  openjdk version "1.8.0_25"
 *	OpenJDK Runtime Environment (build 1.8.0_25-b18)
 *	OpenJDK 64-Bit Server VM (build 25.25-b02, mixed mode)
 *
 * - An Object has a reported length of 16 bytes, but appears to be 12, as an Object with an extra 32 bit int or Object reference also reports as 16.
 * - A String has a reported length of 24 bytes
 * - A BraidImpl with an InternalKnot reference and an int index has a reported length of 24 bytes, but adding up to 4 bytes still results 
 *   in a reported size of 24 bytes. 
 *   
 *   This means that there should be 4 free bytes available for future changes. There is now a cached hascode that takes up these 4 bytes. 
 *   In the future, multiple Baid implementations will likely be provided, each providing only that functionality (such as cached hascodes)  that are requested
 *   from the Knot.
 *   
 *   This also means that if there
 *   were some way to cram the required BraidImpl data into a single 4 bytes, it might mean an 8 byte reduction from 24 to 16 bytes. 
 * 
 */
final class BraidImpl implements Braid {
	
	private final InternalKnot knot;
	private final int index;
	// Cached hashcode of the String.
	private int hash = 0;
	
	BraidImpl(InternalKnot knot, String string) {
		if (knot == null) {
			throw new NullPointerException("null Knot");
		}
		if (string == null) {
			throw new NullPointerException("null string");
		}
		this.knot = knot;
		this.index = this.knot.addString(string);
	}

	@Override
	/**
	 * {@inheritDoc}
	 * 
	 */
	public String get(){
		return knot.lookupString(index);
	}

	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = get().hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object anObject) {
		//TODO: if other instance is a BraidImpl, and knots are the same, just check the indexes, or maybe delegate this to the braid?
		return BraidUtil.equals(this, anObject);
	}

}
