/*
    Seam - Library for Transparent Compression of Java Strings.

    Copyright (C) 2014 James Scriven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.reific.braid;

import java.util.HashMap;
import java.util.Map;

public class MockBuffer implements Buffer {

	int size = 0;
	//	private class Entry {
	//		public Entry(Object value, int length) {
	//			this.value = value;
	//			this.length = length;
	//		}
	//
	//		Object value;
	//		int length;
	//	}
	//
	//	private List<Entry> entries = new ArrayList<Entry>();
	Map<Integer, Object> map = new HashMap<Integer, Object>();

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int nextWritePosition() {
		return size;
	}

	@Override
	public void putVInt(int value) {
		map.put(size++, value);

	}

	@Override
	public void putByte(byte value) {
		map.put(size++, value);

	}

	@Override
	public VInt getVInt(int logicalIndex) {
		return new VInt(1, (int) map.get(logicalIndex));
	}

	@Override
	public byte getByte(int logicalIndex) {
		return (byte) map.get(logicalIndex);
	}

}
