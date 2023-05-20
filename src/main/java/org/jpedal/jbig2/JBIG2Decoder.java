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
* JBIG2Decoder.java
* ---------------
*/
package org.jpedal.jbig2;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jpedal.jbig2.JBIG2Exception;
import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;
import org.jpedal.jbig2.image.JBIG2Bitmap;
import org.jpedal.jbig2.segment.Segment;
import org.jpedal.jbig2.segment.pageinformation.PageInformationSegment;

/**
 * <p>JBIG2Decoder class.</p>
 *
  *
  *
 */
public class JBIG2Decoder {
	
	private final JBIG2StreamDecoder streamDecoder;

	/**
	 * Constructor
	 */
	public JBIG2Decoder() {
		streamDecoder = new JBIG2StreamDecoder();
	}

	/**
	 * If the data stream is taken from a PDF, there may be some global data. Pass any global data
	 * in here.  Call this method before decodeJBIG2(...)
	 *
	 * @param data global data
	 * @throws org.jpedal.jbig2.JBIG2Exception if any.
	 */
	public void setGlobalData(byte[] data) throws org.jpedal.jbig2.JBIG2Exception {
		streamDecoder.setGlobalData(data);
	}

	/**
	 * Decodes a JBIG2 image from a File object
	 *
	 * @param file File to decode
	 * @throws IOException if any.
	 * @throws org.jpedal.jbig2.JBIG2Exception if any.
	 */
	public void decodeJBIG2(File file) throws IOException, org.jpedal.jbig2.JBIG2Exception {
		decodeJBIG2(file.getAbsolutePath());
	}

	/**
	 * Decodes a JBIG2 image from a String path
	 *
	 * @param file Must be the full path to the image
	 * @throws IOException if any.
	 * @throws org.jpedal.jbig2.JBIG2Exception if any.
	 */
	public void decodeJBIG2(String file) throws IOException, org.jpedal.jbig2.JBIG2Exception {
		decodeJBIG2(new FileInputStream(file));
	}

	/**
	 * Decodes a JBIG2 image from an InputStream
	 *
	 * @param inputStream InputStream
	 * @throws IOException if any.
	 * @throws org.jpedal.jbig2.JBIG2Exception if any.
	 */
	public void decodeJBIG2(InputStream inputStream) throws IOException, org.jpedal.jbig2.JBIG2Exception {
		int availiable = inputStream.available();

		byte[] bytes = new byte[availiable];
		inputStream.read(bytes);

		decodeJBIG2(bytes);
	}
	
	/**
	 * Decodes a JBIG2 image from a byte array
	 *
	 * @param data the raw data stream
	 * @throws IOException if any.
	 * @throws org.jpedal.jbig2.JBIG2Exception if any.
	 */
	public void decodeJBIG2(byte[] data) throws IOException, JBIG2Exception {
		streamDecoder.decodeJBIG2(data);
	}

	/**
	 * <p>getPageAsBufferedImage.</p>
	 *
	 * @param page a int.
	 * @return a {@link BufferedImage} object.
	 */
	public BufferedImage getPageAsBufferedImage(int page) {
		page++;
		JBIG2Bitmap pageBitmap = streamDecoder.findPageSegement(page).getPageBitmap();

		byte[] bytes = pageBitmap.getData(true);

		if (bytes == null)
			return null;

		// make a a DEEP copy so we cant alter
		int len = bytes.length;
		byte[] copy = new byte[len];
		System.arraycopy(bytes, 0, copy, 0, len);

		// byte[] data = pageBitmap.getData(true).clone();
		int width = pageBitmap.getWidth();
		int height = pageBitmap.getHeight();

		/** create an image from the raw data */
		DataBuffer db = new DataBufferByte(copy, copy.length);

		WritableRaster raster = Raster.createPackedRaster(db, width, height, 1, null);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		image.setData(raster);


		return image;
	}

	/**
	 * <p>isNumberOfPagesKnown.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isNumberOfPagesKnown() {
		return streamDecoder.isNumberOfPagesKnown();
	}

	/**
	 * <p>getNumberOfPages.</p>
	 *
	 * @return a int.
	 */
	public int getNumberOfPages() {
		int pages = streamDecoder.getNumberOfPages();
		if (streamDecoder.isNumberOfPagesKnown() && pages != 0)
			return pages;

		int noOfPages = 0;

		List<Segment> segments = getAllSegments();
		for (Segment segment : segments) {
			if (segment.getSegmentHeader().getSegmentType() == Segment.PAGE_INFORMATION)
				noOfPages++;
		}

		return noOfPages;
	}

	/**
	 * <p>getAllSegments.</p>
	 *
	 * @return a {@link List} object.
	 */
	public List<Segment> getAllSegments() {
		return streamDecoder.getAllSegments();
	}

	/**
	 * <p>findPageSegement.</p>
	 *
	 * @param page a int.
	 * @return a {@link PageInformationSegment} object.
	 */
	public PageInformationSegment findPageSegement(int page) {
		page++;
		return streamDecoder.findPageSegement(page);
	}

	/**
	 * <p>findSegment.</p>
	 *
	 * @param segmentNumber a int.
	 * @return a {@link Segment} object.
	 */
	public Segment findSegment(int segmentNumber) {
		return streamDecoder.findSegment(segmentNumber);
	}

	/**
	 * <p>getPageAsJBIG2Bitmap.</p>
	 *
	 * @param page a int.
	 * @return a {@link JBIG2Bitmap} object.
	 */
	public JBIG2Bitmap getPageAsJBIG2Bitmap(int page) {
		page++;
		return streamDecoder.findPageSegement(page).getPageBitmap();
	}

	/**
	 * <p>isRandomAccessOrganisationUsed.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRandomAccessOrganisationUsed() {
		return streamDecoder.isRandomAccessOrganisationUsed();
	}
}
