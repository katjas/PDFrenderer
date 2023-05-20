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
* SegmentHeader.java
* ---------------
*/
package org.jpedal.jbig2.segment;

import java.util.logging.Logger;

import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;

/**
 * <p>SegmentHeader class.</p>
 *
  *
  *
 */
public class SegmentHeader {

	private static final Logger logger = Logger.getLogger(SegmentHeader.class.getName());
	private int segmentNumber;

	private int segmentType;
	private boolean pageAssociationSizeSet;
	private boolean deferredNonRetainSet;

	private int referredToSegmentCount;
	private short[] rententionFlags;

	private int[] referredToSegments;
	private int pageAssociation;
	private int dataLength;

	/**
	 * <p>Setter for the field <code>segmentNumber</code>.</p>
	 *
	 * @param SegmentNumber a int.
	 */
	public void setSegmentNumber(int SegmentNumber) {
		this.segmentNumber = SegmentNumber;
	}

	/**
	 * <p>setSegmentHeaderFlags.</p>
	 *
	 * @param SegmentHeaderFlags a short.
	 */
	public void setSegmentHeaderFlags(short SegmentHeaderFlags) {
		segmentType = SegmentHeaderFlags & 63; // 63 = 00111111
		pageAssociationSizeSet = (SegmentHeaderFlags & 64) == 64; // 64 = // 01000000
		deferredNonRetainSet = (SegmentHeaderFlags & 80) == 80; // 64 = 10000000

		if (JBIG2StreamDecoder.debug) {
			logger.info("SegmentType = " + segmentType);
			logger.info("pageAssociationSizeSet = " + pageAssociationSizeSet);
			logger.info("deferredNonRetainSet = " + deferredNonRetainSet);
		}
	}

	/**
	 * <p>Setter for the field <code>referredToSegmentCount</code>.</p>
	 *
	 * @param referredToSegmentCount a int.
	 */
	public void setReferredToSegmentCount(int referredToSegmentCount) {
		this.referredToSegmentCount = referredToSegmentCount;
	}

	/**
	 * <p>Setter for the field <code>rententionFlags</code>.</p>
	 *
	 * @param rententionFlags an array of {@link short} objects.
	 */
	public void setRententionFlags(short[] rententionFlags) {
		this.rententionFlags = rententionFlags;
	}

	/**
	 * <p>Setter for the field <code>referredToSegments</code>.</p>
	 *
	 * @param referredToSegments an array of {@link int} objects.
	 */
	public void setReferredToSegments(int[] referredToSegments) {
		this.referredToSegments = referredToSegments;
	}

	/**
	 * <p>Getter for the field <code>referredToSegments</code>.</p>
	 *
	 * @return an array of {@link int} objects.
	 */
	public int[] getReferredToSegments() {
		return referredToSegments;
	}

	/**
	 * <p>Getter for the field <code>segmentType</code>.</p>
	 *
	 * @return a int.
	 */
	public int getSegmentType() {
		return segmentType;
	}

	/**
	 * <p>Getter for the field <code>segmentNumber</code>.</p>
	 *
	 * @return a int.
	 */
	public int getSegmentNumber() {
		return segmentNumber;
	}

	/**
	 * <p>isPageAssociationSizeSet.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPageAssociationSizeSet() {
		return pageAssociationSizeSet;
	}

	/**
	 * <p>isDeferredNonRetainSet.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDeferredNonRetainSet() {
		return deferredNonRetainSet;
	}

	/**
	 * <p>Getter for the field <code>referredToSegmentCount</code>.</p>
	 *
	 * @return a int.
	 */
	public int getReferredToSegmentCount() {
		return referredToSegmentCount;
	}

	/**
	 * <p>Getter for the field <code>rententionFlags</code>.</p>
	 *
	 * @return an array of {@link short} objects.
	 */
	public short[] getRententionFlags() {
		return rententionFlags;
	}

	/**
	 * <p>Getter for the field <code>pageAssociation</code>.</p>
	 *
	 * @return a int.
	 */
	public int getPageAssociation() {
		return pageAssociation;
	}

	/**
	 * <p>Setter for the field <code>pageAssociation</code>.</p>
	 *
	 * @param pageAssociation a int.
	 */
	public void setPageAssociation(int pageAssociation) {
		this.pageAssociation = pageAssociation;
	}

	/**
	 * <p>Setter for the field <code>dataLength</code>.</p>
	 *
	 * @param dataLength a int.
	 */
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	/**
	 * <p>Setter for the field <code>segmentType</code>.</p>
	 *
	 * @param type a int.
	 */
	public void setSegmentType(int type) {
		this.segmentType = type;
	}

	/**
	 * <p>getSegmentDataLength.</p>
	 *
	 * @return a int.
	 */
	public int getSegmentDataLength() {
		return dataLength;
	}
}
