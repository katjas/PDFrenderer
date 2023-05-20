/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
* Main Developer: Simon Barnett
*
* 	This file is part of JPedal
*
* Copyright (c) 2008, IDRsolutions
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the IDRsolutions nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY IDRsolutions ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL IDRsolutions BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* Other JBIG2 image decoding implementations include
* jbig2dec (http://jbig2dec.sourceforge.net/)
* xpdf (http://www.foolabs.com/xpdf/)
* 
* The final draft JBIG2 specification can be found at http://www.jpeg.org/public/fcd14492.pdf
* 
* All three of the above resources were used in the writing of this software, with methodologies,
* processes and inspiration taken from all three.
*
* ---------------
* ArithmeticDecoder.java
* ---------------
*/
package org.jpedal.jbig2.decoders;

import java.io.IOException;

import org.jpedal.jbig2.decoders.ArithmeticDecoderStats;
import org.jpedal.jbig2.decoders.DecodeIntResult;
import org.jpedal.jbig2.io.StreamReader;
import org.jpedal.jbig2.util.BinaryOperation;

/**
 * <p>ArithmeticDecoder class.</p>
 *
  *
  *
 */
public class ArithmeticDecoder {

	private final StreamReader reader;

	public org.jpedal.jbig2.decoders.ArithmeticDecoderStats genericRegionStats, refinementRegionStats;

	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iadhStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iadwStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iaexStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iaaiStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iadtStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iaitStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iafsStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iadsStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iardxStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iardyStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iardwStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iardhStats;
	public final org.jpedal.jbig2.decoders.ArithmeticDecoderStats iariStats;
	public org.jpedal.jbig2.decoders.ArithmeticDecoderStats iaidStats;

	final int[] contextSize = { 16, 13, 10, 10 };
	final int[] referredToContextSize = { 13, 10 };

	long buffer0, buffer1;
	long c, a;
	long previous;
	
	int counter;

	/**
	 * <p>Constructor for ArithmeticDecoder.</p>
	 *
	 * @param reader a {@link StreamReader} object.
	 */
	public ArithmeticDecoder(StreamReader reader) {
		this.reader = reader;

		genericRegionStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 1);
		refinementRegionStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 1);

		iadhStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iadwStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iaexStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iaaiStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iadtStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iaitStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iafsStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iadsStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iardxStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iardyStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iardwStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iardhStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iariStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 9);
		iaidStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << 1);
	}

	/**
	 * <p>resetIntStats.</p>
	 *
	 * @param symbolCodeLength a int.
	 */
	public void resetIntStats(int symbolCodeLength) {
		iadhStats.reset();
		iadwStats.reset();
		iaexStats.reset();
		iaaiStats.reset();
		iadtStats.reset();
		iaitStats.reset();
		iafsStats.reset();
		iadsStats.reset();
		iardxStats.reset();
		iardyStats.reset();
		iardwStats.reset();
		iardhStats.reset();
		iariStats.reset();

		if (iaidStats.getContextSize() == 1 << (symbolCodeLength + 1)) {
			iaidStats.reset();
		} else {
			iaidStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << (symbolCodeLength + 1));
		}
	}

	/**
	 * <p>resetGenericStats.</p>
	 *
	 * @param template a int.
	 * @param previousStats a {@link org.jpedal.jbig2.decoders.ArithmeticDecoderStats} object.
	 */
	public void resetGenericStats(int template, org.jpedal.jbig2.decoders.ArithmeticDecoderStats previousStats) {
		int size = contextSize[template];

		if (previousStats != null && previousStats.getContextSize() == size) {
			if (genericRegionStats.getContextSize() == size) {
				genericRegionStats.overwrite(previousStats);
			} else {
				genericRegionStats = previousStats.copy();
			}
		} else {
			if (genericRegionStats.getContextSize() == size) {
				genericRegionStats.reset();
			} else {
				genericRegionStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << size);
			}
		}
	}

	/**
	 * <p>resetRefinementStats.</p>
	 *
	 * @param template a int.
	 * @param previousStats a {@link org.jpedal.jbig2.decoders.ArithmeticDecoderStats} object.
	 */
	public void resetRefinementStats(int template, org.jpedal.jbig2.decoders.ArithmeticDecoderStats previousStats) {
		int size = referredToContextSize[template];
		if (previousStats != null && previousStats.getContextSize() == size) {
			if (refinementRegionStats.getContextSize() == size) {
				refinementRegionStats.overwrite(previousStats);
			} else {
				refinementRegionStats = previousStats.copy();
			}
		} else {
			if (refinementRegionStats.getContextSize() == size) {
				refinementRegionStats.reset();
			} else {
				refinementRegionStats = new org.jpedal.jbig2.decoders.ArithmeticDecoderStats(1 << size);
			}
		}
	}

	/**
	 * <p>start.</p>
	 *
	 * @throws IOException if any.
	 */
	public void start() throws IOException {
		buffer0 = reader.readByte();
		buffer1 = reader.readByte();

		c = BinaryOperation.bit32ShiftL((buffer0 ^ 0xff), 16);
		readByte();
		c = BinaryOperation.bit32ShiftL(c, 7);
		counter -= 7;
		a = 0x80000000l;
	}

	/**
	 * <p>decodeInt.</p>
	 *
	 * @param stats a {@link org.jpedal.jbig2.decoders.ArithmeticDecoderStats} object.
	 * @return a {@link org.jpedal.jbig2.decoders.DecodeIntResult} object.
	 * @throws IOException if any.
	 */
	public org.jpedal.jbig2.decoders.DecodeIntResult decodeInt(org.jpedal.jbig2.decoders.ArithmeticDecoderStats stats) throws IOException {
		long value;

		previous = 1;
		int s = decodeIntBit(stats);
		if (decodeIntBit(stats) != 0) {
			if (decodeIntBit(stats) != 0) {
				if (decodeIntBit(stats) != 0) {
					if (decodeIntBit(stats) != 0) {
						if (decodeIntBit(stats) != 0) {
							value = 0;
							for (int i = 0; i < 32; i++) {
								value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
							}
							value += 4436;
						} else {
							value = 0;
							for (int i = 0; i < 12; i++) {
								value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
							}
							value += 340;
						}
					} else {
						value = 0;
						for (int i = 0; i < 8; i++) {
							value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
						}
						value += 84;
					}
				} else {
					value = 0;
					for (int i = 0; i < 6; i++) {
						value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
					}
					value += 20;
				}
			} else {
				value = decodeIntBit(stats);
				value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
				value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
				value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
				value += 4;
			}
		} else {
			value = decodeIntBit(stats);
			value = BinaryOperation.bit32ShiftL(value, 1) | decodeIntBit(stats);
		}

		int decodedInt;
		if (s != 0) {
			if (value == 0) {
				return new org.jpedal.jbig2.decoders.DecodeIntResult((int) value, false);
			}
			decodedInt = (int) -value;
		} else {
			decodedInt = (int) value;
		}

		return new DecodeIntResult(decodedInt, true);
	}

	/**
	 * <p>decodeIAID.</p>
	 *
	 * @param codeLen a long.
	 * @param stats a {@link org.jpedal.jbig2.decoders.ArithmeticDecoderStats} object.
	 * @return a long.
	 * @throws IOException if any.
	 */
	public long decodeIAID(long codeLen, org.jpedal.jbig2.decoders.ArithmeticDecoderStats stats) throws IOException {
		previous = 1;
		for (long i = 0; i < codeLen; i++) {
			int bit = decodeBit(previous, stats);
			previous = BinaryOperation.bit32ShiftL(previous, 1) | bit;
		}

		return previous - (1 << codeLen);
	}

	/**
	 * <p>decodeBit.</p>
	 *
	 * @param context a long.
	 * @param stats a {@link org.jpedal.jbig2.decoders.ArithmeticDecoderStats} object.
	 * @return a int.
	 * @throws IOException if any.
	 */
	public int decodeBit(long context, org.jpedal.jbig2.decoders.ArithmeticDecoderStats stats) throws IOException {
		int iCX = BinaryOperation.bit8Shift(stats.getContextCodingTableValue((int) context), 1, BinaryOperation.RIGHT_SHIFT);
		int mpsCX = stats.getContextCodingTableValue((int) context) & 1;
		int qe = qeTable[iCX];

		a -= qe;

		int bit;
		if (c < a) {
			if ((a & 0x80000000) != 0) {
				bit = mpsCX;
			} else {
				if (a < qe) {
					bit = 1 - mpsCX;
					if (switchTable[iCX] != 0) {
						stats.setContextCodingTableValue((int) context, (nlpsTable[iCX] << 1) | (1 - mpsCX));
					} else {
						stats.setContextCodingTableValue((int) context, (nlpsTable[iCX] << 1) | mpsCX);
					}
				} else {
					bit = mpsCX;
					stats.setContextCodingTableValue((int) context, (nmpsTable[iCX] << 1) | mpsCX);
				}
				do {
					if (counter == 0) {
						readByte();
					}

					a = BinaryOperation.bit32ShiftL(a, 1);
					c = BinaryOperation.bit32ShiftL(c, 1);

					counter--;
				} while ((a & 0x80000000) == 0);
			}
		} else {
			c -= a;

			if (a < qe) {
				bit = mpsCX;
				stats.setContextCodingTableValue((int) context, (nmpsTable[iCX] << 1) | mpsCX);
			} else {
				bit = 1 - mpsCX;
				if (switchTable[iCX] != 0) {
					stats.setContextCodingTableValue((int) context, (nlpsTable[iCX] << 1) | (1 - mpsCX));
				} else {
					stats.setContextCodingTableValue((int) context, (nlpsTable[iCX] << 1) | mpsCX);
				}
			}
			a = qe;

			do {
				if (counter == 0) {
					readByte();
				}

				a = BinaryOperation.bit32ShiftL(a, 1);
				c = BinaryOperation.bit32ShiftL(c, 1);

				counter--;
			} while ((a & 0x80000000) == 0);
		}
		return bit;
	}

	private void readByte() throws IOException {
		if (buffer0 == 0xff) {
			if (buffer1 > 0x8f) {
				counter = 8;
			} else {
				buffer0 = buffer1;
				buffer1 = reader.readByte();
				c = c + 0xfe00 - (BinaryOperation.bit32ShiftL(buffer0, 9));
				counter = 7;
			}
		} else {
			buffer0 = buffer1;
			buffer1 = reader.readByte();
			c = c + 0xff00 - (BinaryOperation.bit32ShiftL(buffer0, 8));
			counter = 8;
		}
	}

	private int decodeIntBit(ArithmeticDecoderStats stats) throws IOException {
		int bit;

		bit = decodeBit(previous, stats);
		if (previous < 0x100) {
			previous = BinaryOperation.bit32ShiftL(previous, 1) | bit;
		} else {
			previous = (((BinaryOperation.bit32ShiftL(previous, 1)) | bit) & 0x1ff) | 0x100;
		}
		return bit;
	}

	final int[] qeTable = { 0x56010000, 0x34010000, 0x18010000, 0x0AC10000, 0x05210000, 0x02210000, 0x56010000, 0x54010000, 0x48010000, 0x38010000, 0x30010000, 0x24010000, 0x1C010000, 0x16010000, 0x56010000, 0x54010000, 0x51010000, 0x48010000, 0x38010000, 0x34010000, 0x30010000, 0x28010000, 0x24010000, 0x22010000, 0x1C010000, 0x18010000, 0x16010000, 0x14010000, 0x12010000, 0x11010000, 0x0AC10000, 0x09C10000, 0x08A10000, 0x05210000, 0x04410000, 0x02A10000, 0x02210000, 0x01410000, 0x01110000, 0x00850000, 0x00490000, 0x00250000, 0x00150000, 0x00090000, 0x00050000, 0x00010000,
			0x56010000 };

	final int[] nmpsTable = { 1, 2, 3, 4, 5, 38, 7, 8, 9, 10, 11, 12, 13, 29, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 45, 46 };

	final int[] nlpsTable = { 1, 6, 9, 12, 29, 33, 6, 14, 14, 14, 17, 18, 20, 21, 14, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 46 };

	final int[] switchTable = { 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
}
