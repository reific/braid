package org.reific.braid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AutoGrowingByteBufferTest {

	@Test
	public void testSmallInitialSize() throws Exception {
		// Actual size will be MIN_SIZE
		Buffer buffer = new AutoGrowingByteBuffer(1, 1.5f, false);
		assertEquals(0, buffer.nextWritePosition());
		buffer.putVInt(99);
		assertEquals(1, buffer.nextWritePosition());
		buffer.putByte((byte) 'a');
		assertEquals(2, buffer.nextWritePosition());
		buffer.putByte((byte) 'b');
		assertEquals(3, buffer.nextWritePosition());
		buffer.putByte((byte) 'c');
		assertEquals(4, buffer.nextWritePosition());
		buffer.putByte((byte) 'd');

		assertEquals(99, value(buffer.getVInt(0)));
		assertEquals('a', buffer.getByte(1));
		assertEquals('b', buffer.getByte(2));
		assertEquals('c', buffer.getByte(3));
		assertEquals('d', buffer.getByte(4));

	}
	@Test
	public void testDirect() throws Exception {
		Buffer buffer = new AutoGrowingByteBuffer(8, 1.5f, true);
		assertEquals(0, buffer.nextWritePosition());
		buffer.putVInt(1);
		assertEquals(1, buffer.nextWritePosition());
		assertEquals(1, value(buffer.getVInt(0)));
	}

	@Test
	public void testSingleFullBuffer() throws Exception {
		Buffer buffer = new AutoGrowingByteBuffer(8, 1.5f, false);
		assertEquals(0, buffer.nextWritePosition());
		buffer.putVInt(1);
		assertEquals(1, buffer.nextWritePosition());
		buffer.putVInt(2);
		assertEquals(2, buffer.nextWritePosition());
		buffer.putByte((byte) 'a');
		buffer.putByte((byte) 'b');
		buffer.putByte((byte) 'c');
		assertEquals(5, buffer.nextWritePosition());
	}

	@Test
	public void testGrowing() throws Exception {
		Buffer buffer = new AutoGrowingByteBuffer(5, 1.5f, false);
		assertEquals(0, buffer.nextWritePosition());
		// put 1 varInt
		buffer.putVInt(1);
		assertEquals(1, buffer.nextWritePosition());
		assertEquals(1, value(buffer.getVInt(0)));
		assertEquals(1, numBytes(buffer.getVInt(0)));

		buffer.putVInt(2);
		assertEquals(2, buffer.nextWritePosition());
		assertEquals(1, value(buffer.getVInt(0)));
		assertEquals(1, numBytes(buffer.getVInt(0)));
		assertEquals(2, value(buffer.getVInt(1)));
		assertEquals(1, numBytes(buffer.getVInt(1)));

		buffer.putVInt(127);
		assertEquals(3, buffer.nextWritePosition());

		// write a VInt with 2 bytes
		buffer.putVInt(128);
		assertEquals(5, buffer.nextWritePosition());
		assertEquals(128, value(buffer.getVInt(3)));
		assertEquals(2, numBytes(buffer.getVInt(3)));

		buffer.putVInt(16_383);
		assertEquals(7, buffer.nextWritePosition());
		assertEquals(16_383, value(buffer.getVInt(5)));
		assertEquals(2, numBytes(buffer.getVInt(5)));

		// 3 bytes
		buffer.putVInt(16_384);
		assertEquals(10, buffer.nextWritePosition());
		assertEquals(16_384, value(buffer.getVInt(7)));
		assertEquals(3, numBytes(buffer.getVInt(7)));

		// 3 bytes 2^21 -1
		buffer.putVInt((int) Math.pow(2, 21) - 1);
		assertEquals(13, buffer.nextWritePosition());
		assertEquals(2097151, value(buffer.getVInt(10)));
		assertEquals(3, numBytes(buffer.getVInt(10)));

		// 4 bytes 2^21
		buffer.putVInt((int) Math.pow(2, 21));
		assertEquals(17, buffer.nextWritePosition());
		assertEquals(2097152, value(buffer.getVInt(13)));
		assertEquals(4, numBytes(buffer.getVInt(13)));

		// 4 bytes 2^28 -1
		buffer.putVInt((int) Math.pow(2, 28) - 1);
		assertEquals(21, buffer.nextWritePosition());
		assertEquals(268_435_455, value(buffer.getVInt(17)));
		assertEquals(4, numBytes(buffer.getVInt(17)));

		// 5 bytes 2^28
		buffer.putVInt((int) Math.pow(2, 28));
		assertEquals(26, buffer.nextWritePosition());
		assertEquals(268_435_456, value(buffer.getVInt(21)));
		assertEquals(5, numBytes(buffer.getVInt(21)));

		// 5 bytes - Max int value (2^31 -1)
		buffer.putVInt(Integer.MAX_VALUE);
		assertEquals(31, buffer.nextWritePosition());
		assertEquals(Integer.MAX_VALUE, value(buffer.getVInt(26)));
		assertEquals(5, numBytes(buffer.getVInt(26)));

	}

	private int value(long vInt) {
		return (int) vInt;
	}

	private int numBytes(long vInt) {
		return (int) (vInt >> 32);
	}

}
