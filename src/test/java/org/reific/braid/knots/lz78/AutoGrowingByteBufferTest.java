package org.reific.braid.knots.lz78;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AutoGrowingByteBufferTest {

	@Test
	public void testSmallInitialSize() throws Exception {
		// Actual size will be LARGEST_DATA_UNIT
		AutoGrowingByteBuffer buffer = new AutoGrowingByteBuffer(1);
		assertEquals(0, buffer.nextWritePosition());
		buffer.putInt(99);
		assertEquals(4, buffer.nextWritePosition());
		buffer.putChar('a');
		assertEquals(6, buffer.nextWritePosition());
		buffer.putChar('b');
		assertEquals(8, buffer.nextWritePosition());
		buffer.putChar('c');
		assertEquals(10, buffer.nextWritePosition());
		buffer.putChar('d');

		assertEquals(99, buffer.getInt(0));
		assertEquals('a', buffer.getChar(4));
		assertEquals('b', buffer.getChar(6));
		assertEquals('c', buffer.getChar(8));
		assertEquals('d', buffer.getChar(10));

	}
	@Test
	public void testSingleFullBuffer() throws Exception {
		AutoGrowingByteBuffer buffer = new AutoGrowingByteBuffer(8);
		assertEquals(0, buffer.nextWritePosition());
		buffer.putInt(1);
		assertEquals(4, buffer.nextWritePosition());
		buffer.putInt(1);
		assertEquals(8, buffer.nextWritePosition());
	}

	@Test
	public void testGrowing() throws Exception {
		AutoGrowingByteBuffer buffer = new AutoGrowingByteBuffer(5);
		assertEquals(0, buffer.nextWritePosition());
		// put 4 bytes
		buffer.putInt(1);
		assertEquals(4, buffer.nextWritePosition());
		assertEquals(1, buffer.getInt(0));

		buffer.putInt(2);
		assertEquals(8, buffer.nextWritePosition());
		assertEquals(1, buffer.getInt(0));
		assertEquals(2, buffer.getInt(4));
	}

}
