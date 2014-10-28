/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.sun.pdfview.decode;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import javax.swing.ImageIcon;

import ch.randelshofer.media.jpeg.JPEGImageIO;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/**
 * decode a DCT encoded array into a byte array.  This class uses Java's
 * built-in JPEG image class to do the decoding.
 *
 * @author Mike Wessler
 */
public class DCTDecode {

    /**
     * decode an array of bytes in DCT format.
     * <p>
     * DCT is the format used by JPEG images, so this class simply
     * loads the DCT-format bytes as an image, then reads the bytes out
     * of the image to create the array.  Unfortunately, their most
     * likely use is to get turned BACK into an image, so this isn't
     * terribly efficient... but is is general... don't hit, please.
     * <p>
     * The DCT-encoded stream may have 1, 3 or 4 samples per pixel, depending
     * on the colorspace of the image.  In decoding, we look for the colorspace
     * in the stream object's dictionary to decide how to decode this image.
     * If no colorspace is present, we guess 3 samples per pixel.
     *
     * @param dict the stream dictionary
     * @param buf the DCT-encoded buffer
     * @param params the parameters to the decoder (ignored)
     * @return the decoded buffer
     * @throws PDFParseException 
     */
    protected static ByteBuffer decode(PDFObject dict, ByteBuffer buf, PDFObject params) throws PDFParseException {
		// BEGIN PATCH W. Randelshofer Completely rewrote decode routine in
		// order to
		// support JPEG images in the CMYK color space.
		BufferedImage bimg = loadImageData(buf);
		byte[] output = ImageDataDecoder.decodeImageData(bimg);
		return ByteBuffer.wrap(output);
		// END PATCH W. Randelshofer Completely rewrote decode routine in order
		// to
		// support JPEG images in the CMYK color space.

    }

	
	/*************************************************************************
	 * @param buf
	 * @return
	 * @throws PDFParseException
	 ************************************************************************/
	
	private static BufferedImage loadImageData(ByteBuffer buf)
			throws PDFParseException {
		buf.rewind();
		byte[] input = new byte[buf.remaining()];
		buf.get(input);
		BufferedImage bimg;
		try {
			try {
				bimg = JPEGImageIO.read(new ByteArrayInputStream(input), false);				
			} catch (IllegalArgumentException colorProfileMismatch) {
				// we experienced this problem with an embedded jpeg
				// that specified a icc color profile with 4 components 
				// but the raster had only 3 bands (apparently YCC encoded)
				Image img = Toolkit.getDefaultToolkit().createImage(input);
				// wait until image is loaded using ImageIcon for convenience
				ImageIcon imageIcon = new ImageIcon(img);
				// copy to buffered image
				bimg = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
				bimg.getGraphics().drawImage(img, 0, 0 , null);
			}			
		} catch (Exception ex) {
			PDFParseException ex2 = new PDFParseException("DCTDecode failed");
			ex2.initCause(ex);
			throw ex2;
		}

		return bimg;
	}
}


/**
 * Image tracker.  I'm not sure why I'm not using the default Java
 * image tracker for this one.
 */
class MyTracker implements ImageObserver {
    boolean done= false;
    
    /**
     * create a new MyTracker that watches this image.  The image
     * will start loading immediately.
     */
    public MyTracker(Image img) {
	img.getWidth(this);
    }
    
    /**
     * More information has come in about the image.
     */
    @Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			       int width, int height) {
	if ((infoflags & (ALLBITS | ERROR | ABORT))!=0) {
	    synchronized(this) {
		this.done= true;
		notifyAll();
	    }
	    return false;
	}
	return true;
    }
    
    /**
     * Wait until the image is done, then return.
     */
    public synchronized void waitForAll() {
	if (!this.done) {
	    try {
		wait();
	    } catch (InterruptedException ie) {}
	}
    }
}