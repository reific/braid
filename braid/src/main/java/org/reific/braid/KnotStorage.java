package org.reific.braid;

public interface KnotStorage {

	int store(String string);
	String lookup(int index);
	long spaceUsed();
	void flush();

}
