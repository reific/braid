package org.reific.braid;

import java.io.DataInput;
import java.io.DataOutput;

final class AutoGrowingByteArray implements Buffer {

	// When an underlying buffer fills, an additional buffer GROWTH_FACTOR times the
	// size of the previous one is added to the pool. This must be at least one, or
	// growth will eventually stop.
	private static final float MIN_GROWTH_FACTOR = 1.0f;
	// Must be at least as big as the largest primitive (5 byte VInt)
	private static final int MIN_SIZE = 4;
	private static final int MAX_NUM_OF_BYTE_BUFFERS = 32;

	private final float growthFactor;
	// ordered list of ByteBuffers of increasing size.
	// data[n+1].length = floor(GROWTH_FACTOR * data[n].length)
	private final byte[][] dataArray = new byte[MAX_NUM_OF_BYTE_BUFFERS][];
	private int currentBuffer = 0;
	private int totalSizeOfPreviousBuffers = 0;

	private int[] positions = new int[MAX_NUM_OF_BYTE_BUFFERS];

	public AutoGrowingByteArray(int initialCapacity, float growthFactor) {
		if (initialCapacity < MIN_SIZE) {
			initialCapacity = MIN_SIZE;
		}
		this.growthFactor = growthFactor < MIN_GROWTH_FACTOR ? MIN_GROWTH_FACTOR : growthFactor;
		dataArray[currentBuffer] = new byte[initialCapacity];
	}

	@Override
	public int getSize() {
		int size = 0;
		for (int i = 0; i <= currentBuffer; i++) {
			size += dataArray[i].length;
		}
		return size;
	}

	/**
	 * 
	 * @return a logical byte position where the start of the next record will
	 *         start
	 */
	@Override
	public int nextWritePosition() {
		return totalSizeOfPreviousBuffers + positions[currentBuffer];
	}

	@Override
	public void putVInt(int value) {
		growIfNeeded(5);
		//		System.out.printf("putInt  %4s %4s %4s\n", dataArray[currentBuffer].length,
		//				totalSizeOfPreviousBuffers + dataArray[currentBuffer][positions[currentBuffer]], value);
		writeVInt(value, currentBuffer);
	}

	@Override
	public void putByte(byte value) {
		growIfNeeded(1);
		//		System.out.printf("putByte %4s %4s %4s\n", dataArray[currentBuffer].length,
		//				totalSizeOfPreviousBuffers + dataArray[currentBuffer][positions[currentBuffer]], (char) value);
		dataArray[currentBuffer][positions[currentBuffer]++] = value;
	}

	@Override
	public long getVInt(int logicalIndex) {
		int bufferNumber = 0;
		while (logicalIndex >= positions[bufferNumber]) {
			logicalIndex -= positions[bufferNumber++];
		}
		return readVInt(logicalIndex, bufferNumber);
	}

	@Override
	public byte getByte(int logicalIndex) {
		int bufferNumber = 0;
		while (logicalIndex >= positions[bufferNumber]) {
			logicalIndex -= positions[bufferNumber++];
		}
		return dataArray[bufferNumber][logicalIndex];
	}

	private void growIfNeeded(int neededSpace) {
		if (positions[currentBuffer] + neededSpace > dataArray[currentBuffer].length) {
			totalSizeOfPreviousBuffers += positions[currentBuffer];
			dataArray[currentBuffer + 1] = new byte[(int) (dataArray[currentBuffer].length * growthFactor)];
			currentBuffer++;

		}
	}

	/**
	   * (Modified code from Apache Lucene)
	   *  
	   * Writes an int in a variable-length format.  Writes between one and
	   * five bytes.  Smaller values take fewer bytes.  Negative numbers are
	   * supported, but should be avoided.
	   * <p>VByte is a variable-length format for positive integers is defined where the
	   * high-order bit of each byte indicates whether more bytes remain to be read. The
	   * low-order seven bits are appended as increasingly more significant bits in the
	   * resulting integer value. Thus values from zero to 127 may be stored in a single
	   * byte, values from 128 to 16,383 may be stored in two bytes, and so on.</p>
	   * <p>VByte Encoding Example</p>
	   * <table cellspacing="0" cellpadding="2" border="0">
	   * <col width="64*">
	   * <col width="64*">
	   * <col width="64*">
	   * <col width="64*">
	   * <tr valign="top">
	   *   <th align="left" width="25%">Value</th>
	   *   <th align="left" width="25%">Byte 1</th>
	   *   <th align="left" width="25%">Byte 2</th>
	   *   <th align="left" width="25%">Byte 3</th>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">0</td>
	   *   <td width="25%"><kbd>00000000</kbd></td>
	   *   <td width="25%"></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">1</td>
	   *   <td width="25%"><kbd>00000001</kbd></td>
	   *   <td width="25%"></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">2</td>
	   *   <td width="25%"><kbd>00000010</kbd></td>
	   *   <td width="25%"></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr>
	   *   <td valign="top" width="25%">...</td>
	   *   <td valign="bottom" width="25%"></td>
	   *   <td valign="bottom" width="25%"></td>
	   *   <td valign="bottom" width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">127</td>
	   *   <td width="25%"><kbd>01111111</kbd></td>
	   *   <td width="25%"></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">128</td>
	   *   <td width="25%"><kbd>10000000</kbd></td>
	   *   <td width="25%"><kbd>00000001</kbd></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">129</td>
	   *   <td width="25%"><kbd>10000001</kbd></td>
	   *   <td width="25%"><kbd>00000001</kbd></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">130</td>
	   *   <td width="25%"><kbd>10000010</kbd></td>
	   *   <td width="25%"><kbd>00000001</kbd></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr>
	   *   <td valign="top" width="25%">...</td>
	   *   <td width="25%"></td>
	   *   <td width="25%"></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">16,383</td>
	   *   <td width="25%"><kbd>11111111</kbd></td>
	   *   <td width="25%"><kbd>01111111</kbd></td>
	   *   <td width="25%"></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">16,384</td>
	   *   <td width="25%"><kbd>10000000</kbd></td>
	   *   <td width="25%"><kbd>10000000</kbd></td>
	   *   <td width="25%"><kbd>00000001</kbd></td>
	   * </tr>
	   * <tr valign="bottom">
	   *   <td width="25%">16,385</td>
	   *   <td width="25%"><kbd>10000001</kbd></td>
	   *   <td width="25%"><kbd>10000000</kbd></td>
	   *   <td width="25%"><kbd>00000001</kbd></td>
	   * </tr>
	   * <tr>
	   *   <td valign="top" width="25%">...</td>
	   *   <td valign="bottom" width="25%"></td>
	   *   <td valign="bottom" width="25%"></td>
	   *   <td valign="bottom" width="25%"></td>
	   * </tr>
	   * </table>
	   * <p>This provides compression while still being efficient to decode.</p>
	   * 
	   * @param i Smaller values take fewer bytes.  Negative numbers are
	   * supported, but should be avoided.
	 * @param current2 
	 * @return 
	   * @see DataInput#readVInt()
	   */
	private void writeVInt(int i, int bufNum) {
		while ((i & ~0x7F) != 0) {
			dataArray[bufNum][positions[bufNum]++] = (byte) ((i & 0x7F) | 0x80);
			i >>>= 7;
		}
		dataArray[bufNum][positions[bufNum]++] = (byte) i;
	}

	/** 
	 * (Modified code from Apache Lucene) 
	 * 
	 * Reads an int stored in variable-length format.  Reads between one and
	 * five bytes.  Smaller values take fewer bytes.  Negative numbers are not
	 * supported.
	 * <p>
	 * The format is described further in {@link DataOutput#writeVInt(int)}.
	 *
	 * @return a long with the high-order 4 bytes representing the length, in bytes of the VInt that was read from the compressed 
	 * buffer, and the low-order 4 bytes representing the int itself.
	 * 
	 * @see DataOutput#writeVInt(int)
	 */
	private long readVInt(int index, int bufferNumber) {
		/* This is the original code of this method,
		 * but a Hotspot bug (see LUCENE-2975) corrupts the for-loop if
		 * readByte() is inlined. So the loop was unwinded!
		 * <code>
		byte b = readByte();
		int i = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
		  b = readByte();
		  i |= (b & 0x7F) << shift;
		}
		return i;
		* </code>
		*/
		byte b = dataArray[bufferNumber][index++];
		if (b >= 0)
			return (long) 1 << 32 | b & 0xFFFFFFFFL;
		int i = b & 0x7F;
		b = dataArray[bufferNumber][index++];
		i |= (b & 0x7F) << 7;
		if (b >= 0)
			return (long) 2 << 32 | i & 0xFFFFFFFFL;
		b = dataArray[bufferNumber][index++];
		i |= (b & 0x7F) << 14;
		if (b >= 0)
			return (long) 3 << 32 | i & 0xFFFFFFFFL;
		b = dataArray[bufferNumber][index++];
		i |= (b & 0x7F) << 21;
		if (b >= 0)
			return (long) 4 << 32 | i & 0xFFFFFFFFL;
		b = dataArray[bufferNumber][index++];
		// Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
		i |= (b & 0x0F) << 28;
		if ((b & 0xF0) == 0)
			return (long) 5 << 32 | i & 0xFFFFFFFFL;
		throw new RuntimeException("Invalid vInt detected (too many bits)");
	}

}
