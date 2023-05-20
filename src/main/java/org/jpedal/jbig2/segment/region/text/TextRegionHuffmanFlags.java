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
* TextRegionHuffmanFlags.java
* ---------------
*/
package org.jpedal.jbig2.segment.region.text;

import java.util.logging.Logger;

import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;
import org.jpedal.jbig2.segment.Flags;

/**
 * <p>TextRegionHuffmanFlags class.</p>
 *
  *
  *
 */
public class TextRegionHuffmanFlags extends Flags {

	private static final Logger logger = Logger.getLogger(TextRegionHuffmanFlags.class.getName());
	/** Constant <code>SB_HUFF_FS="SB_HUFF_FS"</code> */
	public static final String SB_HUFF_FS = "SB_HUFF_FS";
	/** Constant <code>SB_HUFF_DS="SB_HUFF_DS"</code> */
	public static final String SB_HUFF_DS = "SB_HUFF_DS";
	/** Constant <code>SB_HUFF_DT="SB_HUFF_DT"</code> */
	public static final String SB_HUFF_DT = "SB_HUFF_DT";
	/** Constant <code>SB_HUFF_RDW="SB_HUFF_RDW"</code> */
	public static final String SB_HUFF_RDW = "SB_HUFF_RDW";
	/** Constant <code>SB_HUFF_RDH="SB_HUFF_RDH"</code> */
	public static final String SB_HUFF_RDH = "SB_HUFF_RDH";
	/** Constant <code>SB_HUFF_RDX="SB_HUFF_RDX"</code> */
	public static final String SB_HUFF_RDX = "SB_HUFF_RDX";
	/** Constant <code>SB_HUFF_RDY="SB_HUFF_RDY"</code> */
	public static final String SB_HUFF_RDY = "SB_HUFF_RDY";
	/** Constant <code>SB_HUFF_RSIZE="SB_HUFF_RSIZE"</code> */
	public static final String SB_HUFF_RSIZE = "SB_HUFF_RSIZE";

	/** {@inheritDoc} */
	public void setFlags(int flagsAsInt) {
		this.flagsAsInt = flagsAsInt;

		/** extract SB_HUFF_FS */
		flags.put(SB_HUFF_FS, flagsAsInt & 3);

		/** extract SB_HUFF_DS */
		flags.put(SB_HUFF_DS, (flagsAsInt >> 2) & 3);

		/** extract SB_HUFF_DT */
		flags.put(SB_HUFF_DT, (flagsAsInt >> 4) & 3);

		/** extract SB_HUFF_RDW */
		flags.put(SB_HUFF_RDW, (flagsAsInt >> 6) & 3);

		/** extract SB_HUFF_RDH */
		flags.put(SB_HUFF_RDH, (flagsAsInt >> 8) & 3);

		/** extract SB_HUFF_RDX */
		flags.put(SB_HUFF_RDX, (flagsAsInt >> 10) & 3);

		/** extract SB_HUFF_RDY */
		flags.put(SB_HUFF_RDY, (flagsAsInt >> 12) & 3);

		/** extract SB_HUFF_RSIZE */
		flags.put(SB_HUFF_RSIZE, (flagsAsInt >> 14) & 1);

		if (JBIG2StreamDecoder.debug)
			logger.info("flags: " + flags);
	}
}
