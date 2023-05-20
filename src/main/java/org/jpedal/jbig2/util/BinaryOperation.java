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
* BinaryOperation.java
* ---------------
*/
package org.jpedal.jbig2.util;

/**
 * <p>BinaryOperation class.</p>
 *
  *
  *
 */
public class BinaryOperation {

	/** Constant <code>LEFT_SHIFT=0</code> */
	public static final int LEFT_SHIFT = 0;
	/** Constant <code>RIGHT_SHIFT=1</code> */
	public static final int RIGHT_SHIFT = 1;
	
	/** Constant <code>LONGMASK=0xffffffffl</code> */
	public static final long LONGMASK = 0xffffffffl; // 1111 1111 1111 1111 1111 1111 1111 1111
	/** Constant <code>INTMASK=0xff</code> */
	public static final int INTMASK = 0xff; // 1111 1111

	/**
	 * <p>getInt32.</p>
	 *
	 * @param number an array of {@link short} objects.
	 * @return a int.
	 */
	public static int getInt32(final short[] number) {
		return (number[0] << 24) | (number[1] << 16) | (number[2] << 8) | number[3];
	}

	/**
	 * <p>getInt16.</p>
	 *
	 * @param number an array of {@link short} objects.
	 * @return a int.
	 */
	public static int getInt16(final short[] number) {
		return (number[0] << 8) | number[1];
	}

	/**
	 * <p>bit32ShiftL.</p>
	 *
	 * @param number a long.
	 * @param shift a int.
	 * @return a long.
	 */
	public static long bit32ShiftL(long number, int shift) {
		//return (number << shift) & LONGMASK;
		return number << shift;
	}

	/**
	 * <p>bit32ShiftR.</p>
	 *
	 * @param number a long.
	 * @param shift a int.
	 * @return a long.
	 */
	public static long bit32ShiftR(long number, int shift) {
		//return (number >> shift) & LONGMASK;
		return number >> shift;
	}

	/*public static final  long bit32Shift(long number, int shift, int direction) {
		if (direction == LEFT_SHIFT)
			number <<= shift;
		else
			number >>= shift;

		return (number & LONGMASK);
	}*/

	/**
	 * <p>bit8Shift.</p>
	 *
	 * @param number a int.
	 * @param shift a int.
	 * @param direction a int.
	 * @return a int.
	 */
	public static int bit8Shift(int number, int shift, int direction) {
		if (direction == LEFT_SHIFT)
			number <<= shift;
		else
			number >>= shift;

		return (number & INTMASK);
	}

	/**
	 * <p>getInt32.</p>
	 *
	 * @param number an array of {@link byte} objects.
	 * @return a int.
	 */
	public static int getInt32(final byte[] number) {
		return (number[0] << 24) | (number[1] << 16) | (number[2] << 8) | number[3];
	}
}
