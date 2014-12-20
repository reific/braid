package org.reific.braid;

public interface Knot {

	long spaceUsed();
	Braid braid(String value);
	MutableBraid mutableBraid(String string);
	void flush();

}
