package org.reific.braid.knots.lz78;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AutoGrowingByteBuffer {

	private static final float GROWTH_FACTOR = 1.5f;
	private static final int MIN_SIZE = 64;

	// ordered list of ByteBuffers of increasing size.
	// data[n+1].length = floor(GROWTH_FACTOR * data[n].length)
	private final List<ByteBuffer> data = new ArrayList<ByteBuffer>();
	private ByteBuffer current;
	private int totalSizeOfPreviousBuffers = 0;

	private class PhysicalIndex {
		int index;
		ByteBuffer buffer;

		public PhysicalIndex(int index, ByteBuffer buffer) {
			this.index = index;
			this.buffer = buffer;
		}
	}
	public AutoGrowingByteBuffer(int initialSize) {
		if (initialSize < MIN_SIZE) {
			initialSize = MIN_SIZE;
		}
		current = ByteBuffer.allocate(initialSize);
		data.add(current);
	}

	/**
	 * 
	 * @return a logical byte position where the start of the next record will
	 *         start
	 */
	public int nextWritePosition() {
		return totalSizeOfPreviousBuffers + current.position();
	}

	public void putInt(int value) {
		growIfNeeded(4);
		System.out.printf("putInt %s %s %s\n", current.capacity(),
				current.position(), value);
		current.putInt(value);
	}

	public void putChar(char value) {
		growIfNeeded(2);
		System.out.printf("putChar %s %s %s\n", current.capacity(),
				current.position(), value);
		current.putChar(value);
	}

	public int getInt(int logicalIndex) {
		PhysicalIndex physicalIndex = calculatePhysicalIndex(logicalIndex);
		return physicalIndex.buffer.getInt(physicalIndex.index);
	}

	public char getChar(int logicalIndex) {
		PhysicalIndex physicalIndex = calculatePhysicalIndex(logicalIndex);
		return physicalIndex.buffer.getChar(physicalIndex.index);
	}

	private PhysicalIndex calculatePhysicalIndex(int logicalIndex) {
		int i = 0;
		ByteBuffer buffer = data.get(i++);
		while (logicalIndex >= buffer.position()) {
			logicalIndex -= buffer.position();
			buffer = data.get(i++);
		}
		return new PhysicalIndex(logicalIndex, buffer);
	}

	private void growIfNeeded(int neededSpace) {
		if (current.position() + neededSpace > current.capacity()) {
			totalSizeOfPreviousBuffers += current.position();
			current = ByteBuffer
					.allocate((int) (current.capacity() * GROWTH_FACTOR));
			data.add(current);

		}
	}

}
