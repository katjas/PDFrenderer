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
* PDFSegment.java
* ---------------
*/
package org.jpedal.jbig2.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * <p>PDFSegment class.</p>
 *
  *
  *
 */
public class PDFSegment {

	private final ByteArrayOutputStream header = new ByteArrayOutputStream();
	private final ByteArrayOutputStream data = new ByteArrayOutputStream();
	private int segmentDataLength;

	/**
	 * <p>writeToHeader.</p>
	 *
	 * @param bite a short.
	 */
	public void writeToHeader(short bite) {
		header.write(bite);
	}

	/**
	 * <p>writeToHeader.</p>
	 *
	 * @param bites an array of {@link short} objects.
	 * @throws IOException if any.
	 */
	public void writeToHeader(short[] bites) throws IOException {
		for (short bite : bites) {
			header.write(bite);
		}	
	}

	/**
	 * <p>writeToData.</p>
	 *
	 * @param bite a short.
	 */
	public void writeToData(short bite) {
		data.write(bite);
	}

	/**
	 * <p>Getter for the field <code>header</code>.</p>
	 *
	 * @return a {@link ByteArrayOutputStream} object.
	 */
	public ByteArrayOutputStream getHeader() {
		return header;
	}

	/**
	 * <p>Getter for the field <code>data</code>.</p>
	 *
	 * @return a {@link ByteArrayOutputStream} object.
	 */
	public ByteArrayOutputStream getData() {
		return data;
	}

	/**
	 * <p>setDataLength.</p>
	 *
	 * @param segmentDataLength a int.
	 */
	public void setDataLength(int segmentDataLength) {
		this.segmentDataLength = segmentDataLength;

	}

	/**
	 * <p>Getter for the field <code>segmentDataLength</code>.</p>
	 *
	 * @return a int.
	 */
	public int getSegmentDataLength() {
		return segmentDataLength;
	}
}
