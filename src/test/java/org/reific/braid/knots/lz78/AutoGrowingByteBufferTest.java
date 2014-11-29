package org.reific.braid.knots.lz78;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AutoGrowingByteBufferTest {

	@Test
	public void testSmallInitialSize() throws Exception {
		// Actual size will be MIN_SIZE
		AutoGrowingByteBuffer buffer = new AutoGrowingByteBuffer(1);
		assertEquals(0, buffer.nextWritePosition());
		buffer.putVInt(99);
		assertEquals(1, buffer.nextWritePosition());
		buffer.putChar('a');
		assertEquals(3, buffer.nextWritePosition());
		buffer.putChar('b');
		assertEquals(5, buffer.nextWritePosition());
		buffer.putChar('c');
		assertEquals(7, buffer.nextWritePosition());
		buffer.putChar('d');

		assertEquals(99, buffer.getVInt(0).value);
		assertEquals('a', buffer.getChar(1));
		assertEquals('b', buffer.getChar(3));
		assertEquals('c', buffer.getChar(5));
		assertEquals('d', buffer.getChar(7));

	}
	@Test
	public void testSingleFullBuffer() throws Exception {
		AutoGrowingByteBuffer buffer = new AutoGrowingByteBuffer(8);
		assertEquals(0, buffer.nextWritePosition());
		buffer.putVInt(1);
		assertEquals(1, buffer.nextWritePosition());
		buffer.putVInt(2);
		assertEquals(2, buffer.nextWritePosition());
		buffer.putChar('a');
		buffer.putChar('b');
		buffer.putChar('c');
		assertEquals(8, buffer.nextWritePosition());
	}

	@Test
	public void testGrowing() throws Exception {
		AutoGrowingByteBuffer buffer = new AutoGrowingByteBuffer(5);
		assertEquals(0, buffer.nextWritePosition());
		// put 1 varInt
		buffer.putVInt(1);
		assertEquals(1, buffer.nextWritePosition());
		assertEquals(1, buffer.getVInt(0).value);
		assertEquals(1, buffer.getVInt(0).numBytes);

		buffer.putVInt(2);
		assertEquals(2, buffer.nextWritePosition());
		assertEquals(1, buffer.getVInt(0).value);
		assertEquals(1, buffer.getVInt(0).numBytes);
		assertEquals(2, buffer.getVInt(1).value);
		assertEquals(1, buffer.getVInt(1).numBytes);

		buffer.putVInt(127);
		assertEquals(3, buffer.nextWritePosition());

		// write a VInt with 2 bytes
		buffer.putVInt(128);
		assertEquals(5, buffer.nextWritePosition());
		assertEquals(128, buffer.getVInt(3).value);
		assertEquals(2, buffer.getVInt(3).numBytes);

		buffer.putVInt(16_383);
		assertEquals(7, buffer.nextWritePosition());
		assertEquals(16_383, buffer.getVInt(5).value);
		assertEquals(2, buffer.getVInt(5).numBytes);

		// 3 bytes
		buffer.putVInt(16_384);
		assertEquals(10, buffer.nextWritePosition());
		assertEquals(16_384, buffer.getVInt(7).value);
		assertEquals(3, buffer.getVInt(7).numBytes);

		// 3 bytes 2^21 -1
		buffer.putVInt((int) Math.pow(2, 21) - 1);
		assertEquals(13, buffer.nextWritePosition());
		assertEquals(2097151, buffer.getVInt(10).value);
		assertEquals(3, buffer.getVInt(10).numBytes);

		// 4 bytes 2^21
		buffer.putVInt((int) Math.pow(2, 21));
		assertEquals(17, buffer.nextWritePosition());
		assertEquals(2097152, buffer.getVInt(13).value);
		assertEquals(4, buffer.getVInt(13).numBytes);

		// 4 bytes 2^28 -1
		buffer.putVInt((int) Math.pow(2, 28) - 1);
		assertEquals(21, buffer.nextWritePosition());
		assertEquals(268_435_455, buffer.getVInt(17).value);
		assertEquals(4, buffer.getVInt(17).numBytes);

		// 5 bytes 2^28
		buffer.putVInt((int) Math.pow(2, 28));
		assertEquals(26, buffer.nextWritePosition());
		assertEquals(268_435_456, buffer.getVInt(21).value);
		assertEquals(5, buffer.getVInt(21).numBytes);

		// 5 bytes - Max int value (2^31 -1)
		buffer.putVInt(Integer.MAX_VALUE);
		assertEquals(31, buffer.nextWritePosition());
		assertEquals(Integer.MAX_VALUE, buffer.getVInt(26).value);
		assertEquals(5, buffer.getVInt(26).numBytes);

	}

}
