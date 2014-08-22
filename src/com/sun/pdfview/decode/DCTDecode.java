/*
 * $Id: DCTDecode.java,v 1.3 2010-06-14 17:32:08 lujke Exp $
 *
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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.colorspace.PDFColorSpace;

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
     * of the image to create the array.  If this were to be used
     * against image objects we'd end up wasting a lot of work, because
     * we'd be generating a buffered image here, writing out the bytes,
     * and then generating a buffered image again from those bytes in the
     * PDFImage class.
     * <p>
     * Luckily, the image processing has been optimised to detect
     * DCT decodes at the end of filters, in which case it avoids
     * running the stream through this filter, and just directly
     * generates a BufferedImage from the DCT encoded byte stream.
     * As such, this decode will be invoked only if there's been
     * some very unusual employment of filters in the PDF - e.g.,
     * DCTDecode applied to non-image data, or if DCTDecode is not at
     * the end of a Filter dictionary entry. This is permissible but
     * unlikely to occur in practice.
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
     */
    protected static ByteBuffer decode(PDFObject dict, ByteBuffer buf,
        PDFObject params) throws PDFParseException
    {
	//	System.out.println("DCTDecode image info: "+params);
        buf.rewind();
        
        // copy the data into a byte array required by createimage
        byte[] ary = new byte[buf.remaining()];
        buf.get(ary);
        
        // wait for the image to get drawn
	Image img= Toolkit.getDefaultToolkit().createImage(ary);
	MyTracker mt= new MyTracker(img);
	mt.waitForAll();
        
        // the default components per pixel is 3
        int numComponents = 3;
        
        // see if we have a colorspace
        try {
            PDFObject csObj = dict.getDictRef("ColorSpace");
            if (csObj != null) {
                // we do, so get the number of components
                PDFColorSpace cs = PDFColorSpace.getColorSpace(csObj, null);
                numComponents = cs.getNumComponents();
            }
        } catch (IOException ioe) {
            // oh well
        }
        
        
        // figure out the type
        int type = BufferedImage.TYPE_INT_RGB;
        if (numComponents == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (numComponents == 4) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        
        // create a buffered image
        BufferedImage bimg = new BufferedImage(img.getWidth(null),
					       img.getHeight(null),
					       type);
        Graphics bg= bimg.getGraphics();
        
        // draw the image onto it
	bg.drawImage(img, 0, 0, null);
        
	byte[] output = null;

        // incidentally, there's a bit of an optimisation we could apply here,
        // if we weren't pretty confident that this isn't actually going to
        // be called, anyway. Namely, if we just use JAI to read in the data
        // the underlying data buffer seems to typically be byte[] based,
        // and probably already in the desired arrangement (and if not, that
        // could be engineered by supplying our own sample model). As it is,
        // we won't bother, since this code is most likely not going
        // to be used.        

        if (type == BufferedImage.TYPE_INT_RGB) {
            // read back the data
            DataBufferInt db = (DataBufferInt) bimg.getData().getDataBuffer();
            int[] data = db.getData();
        
            output = new byte[data.length*3];
            for (int i=0; i<data.length; i++) {
                output[i*3]= (byte)(data[i]>>16);
                output[i*3+1]= (byte)(data[i]>>8);
                output[i*3+2]= (byte)(data[i]);
            }
        } else if (type == BufferedImage.TYPE_BYTE_GRAY) {
            DataBufferByte db = (DataBufferByte) bimg.getData().getDataBuffer();
            output = db.getData(); 
        } else if (type == BufferedImage.TYPE_INT_ARGB) {
            // read back the data
            DataBufferInt db = (DataBufferInt) bimg.getData().getDataBuffer();
            int[] data = db.getData();
        
            output = new byte[data.length*4];
            for (int i=0; i<data.length; i++) {
                output[i*4]= (byte)(data[i]>>24);
                output[i*4+1]= (byte)(data[i]>>16);
                output[i*4+2]= (byte)(data[i]>>8);
                output[i*4+3]= (byte)(data[i]);
            }
        }
        
	//	System.out.println("Translated data");
	return ByteBuffer.wrap(output);
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
		done= true;
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
	if (!done) {
	    try {
		wait();
	    } catch (InterruptedException ie) {}
	}
    }
}

