package org.reific.braid;

public interface Knot {

	int getCompressedSize();
	Braid braid(String value);
	MutableBraid mutableBraid(String string);

}
