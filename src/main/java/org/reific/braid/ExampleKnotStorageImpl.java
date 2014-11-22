package org.reific.braid;

import java.util.HashMap;
import java.util.Map;

public class ExampleKnotStorageImpl implements KnotStorage {

	int counter = 0;
	private final Map<Integer,String> map =  new HashMap<Integer, String>();
	@Override
	public int store(String string) {
		map.put(counter, string);
		return counter++;
	}

	@Override
	public String lookup(int index) {
		//return a copy of the string, so that test case pass asserting assertNotSame, since this will be the real behavior
		return new String(map.get(index));
	}

}
