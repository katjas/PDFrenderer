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
package com.sun.pdfview;

import java.awt.Color;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.pdfview.colorspace.AltColorSpace;
import com.sun.pdfview.colorspace.AlternateColorSpace;
import com.sun.pdfview.colorspace.IndexedColor;
import com.sun.pdfview.colorspace.PDFColorSpace;
import com.sun.pdfview.function.FunctionType0;

/**
 * Encapsulates a PDF Image
 */
public class PDFImage {

    public static void dump(PDFObject obj) throws IOException {
        p("dumping PDF object: " + obj);
        if (obj == null) {
            return;
        }
        HashMap dict = obj.getDictionary();
        p("   dict = " + dict);
        for (Object key : dict.keySet()) {
            p("key = " + key + " value = " + dict.get(key));
        }
    }

    public static void p(String string) {
        System.out.println(string);
    }
    /** color key mask. Array of start/end pairs of ranges of color components to
     *  mask out. If a component falls within any of the ranges it is clear. */
    private int[] colorKeyMask = null;
    /** the width of this image in pixels */
    private int width;
    /** the height of this image in pixels */
    private int height;
    /** the colorspace to interpret the samples in */
    private PDFColorSpace colorSpace;
    /** the number of bits per sample component */
    private int bpc;
    /** whether this image is a mask or not */
    private boolean imageMask = false;
    /** the SMask image, if any */
    private PDFImage sMask;
    /** the decode array */
    private float[] decode;
    /** the actual image data */
    private PDFObject imageObj;

    /** 
     * Create an instance of a PDFImage
     */
    protected PDFImage(PDFObject imageObj) {
        this.imageObj = imageObj;
    }

    /**
     * Read a PDFImage from an image dictionary and stream
     *
     * @param obj the PDFObject containing the image's dictionary and stream
     * @param resources the current resources
     * @param useAsSMask - flag for switching colors in case image is used as sMask internally
     * 					   this is needed for handling transparency in smask images.	
     */
    public static PDFImage createImage(PDFObject obj, Map resources, boolean useAsSMask)
            throws IOException {
        // create the image
        PDFImage image = new PDFImage(obj);

        // get the width (required)
        PDFObject widthObj = obj.getDictRef("Width");
        if (widthObj == null) {
            throw new PDFParseException("Unable to read image width: " + obj);
        }
        image.setWidth(widthObj.getIntValue());

        // get the height (required)
        PDFObject heightObj = obj.getDictRef("Height");
        if (heightObj == null) {
            throw new PDFParseException("Unable to get image height: " + obj);
        }
        image.setHeight(heightObj.getIntValue());

        // figure out if we are an image mask (optional)
        PDFObject imageMaskObj = obj.getDictRef("ImageMask");
        if (imageMaskObj != null) {
            image.setImageMask(imageMaskObj.getBooleanValue());
        }
        // read the bpc and colorspace (required except for masks) 
        if (image.isImageMask()) {
            image.setBitsPerComponent(1);
            // create the indexed color space for the mask
            // [PATCHED by michal.busta@gmail.com] - default value od Decode according to PDF spec. is [0, 1]
        	// so the color arry should be:            
            // [PATCHED by XOND] - switched colors in case the image is used as SMask for another image, otherwise transparency isn't 
            //					   handled correctly.
            Color[] colors = useAsSMask? new Color[]{Color.WHITE, Color.BLACK}:new Color[]{Color.BLACK, Color.WHITE};
            PDFObject imageMaskDecode = obj.getDictRef("Decode");
            if (imageMaskDecode != null) {
                PDFObject[] decodeArray = imageMaskDecode.getArray();
                float decode0 = decodeArray[0].getFloatValue();
                if (decode0 == 1.0f) {
                    colors = useAsSMask? new Color[]{Color.BLACK, Color.WHITE}:new Color[]{Color.WHITE, Color.BLACK};
                }
                
/*                float[] decode = new float[decodeArray.length];
                for (int i = 0; i < decodeArray.length; i++) {
                    decode[i] = decodeArray[i].getFloatValue();
                }
                image.setDecode(decode);*/
            }
            
            image.setColorSpace(new IndexedColor(colors));
        } else {
            // get the bits per component (required)
            PDFObject bpcObj = obj.getDictRef("BitsPerComponent");
            if (bpcObj == null) {
                throw new PDFParseException("Unable to get bits per component: " + obj);
            }
            image.setBitsPerComponent(bpcObj.getIntValue());

            // get the color space (required)
            PDFObject csObj = obj.getDictRef("ColorSpace");
            if (csObj == null) {
                throw new PDFParseException("No ColorSpace for image: " + obj);
            }

            PDFColorSpace cs = PDFColorSpace.getColorSpace(csObj, resources);
            image.setColorSpace(cs);

            // read the decode array
            PDFObject decodeObj = obj.getDictRef("Decode");
            if (decodeObj != null) {
                PDFObject[] decodeArray = decodeObj.getArray();

                float[] decode = new float[decodeArray.length];
                for (int i = 0; i < decodeArray.length; i++) {
                    decode[i] = decodeArray[i].getFloatValue();
                }

                image.setDecode(decode);
            }
            
	        // read the soft mask.
	        // If ImageMask is true, this entry must not be present.
	        // (See implementation note 52 in Appendix H.)
            PDFObject sMaskObj = obj.getDictRef("SMask");
            if (sMaskObj == null) {
                // try the explicit mask, if there is no SoftMask
                sMaskObj = obj.getDictRef("Mask");
            }

            if (sMaskObj != null) {
                if (sMaskObj.getType() == PDFObject.STREAM) {
                    try {
                        PDFImage sMaskImage = PDFImage.createImage(sMaskObj, resources, true);
                        image.setSMask(sMaskImage);
                    } catch (IOException ex) {
                        p("ERROR: there was a problem parsing the mask for this object");
                        dump(obj);
                        ex.printStackTrace(System.out);
                    }
                } else if (sMaskObj.getType() == PDFObject.ARRAY) {
                    // retrieve the range of the ColorKeyMask
                    // colors outside this range will not be painted.
                    try {
                        image.setColorKeyMask(sMaskObj);
                    } catch (IOException ex) {
                        p("ERROR: there was a problem parsing the color mask for this object");
                        dump(obj);
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }

        return image;
    }

    /**
     * Get the image that this PDFImage generates.
     *
     * @return a buffered image containing the decoded image data
     */
    public BufferedImage getImage() {
        try {
            BufferedImage bi = (BufferedImage) this.imageObj.getCache();

            if (bi == null) {
                // parse the stream data into an actual image
                bi = parseData(this.imageObj.getStream());
                this.imageObj.setCache(bi);
            }
//            if(bi != null)
//            	ImageIO.write(bi, "jpeg", new File("D:/tmp/pdfImage_getImage.jpg"));
            return bi;
        } catch (IOException ioe) {
            System.out.println("Error reading image");
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * <p>Parse the image stream into a buffered image.  Note that this is
     * guaranteed to be called after all the other setXXX methods have been 
     * called.</p>
     *
     * <p>NOTE: the color convolving is extremely slow on large images.
     * It would be good to see if it could be moved out into the rendering
     * phases, where we might be able to scale the image down first.</p
     */
    protected BufferedImage parseData(byte[] data) {
        // create the data buffer
        DataBuffer db = new DataBufferByte(data, data.length);

        // pick a color model, based on the number of components and
        // bits per component
        ColorModel cm = getColorModel();

        // create a compatible raster
        SampleModel sm = 
                cm.createCompatibleSampleModel (getWidth (), getHeight ());
        WritableRaster raster;
        try {
            raster =
                Raster.createWritableRaster (sm, db, new Point (0, 0));
        } catch (RasterFormatException e) {
            int tempExpectedSize =
                getWidth () * getHeight () *
                getColorSpace ().getNumComponents () *
                Math.max(8,getBitsPerComponent()) / 8;

        	if(tempExpectedSize<3) {
        		tempExpectedSize = 3;
        	}
        	if (tempExpectedSize > data.length) {
                byte[] tempLargerData = new byte[tempExpectedSize];
                System.arraycopy (data, 0, tempLargerData, 0, data.length);
                db = new DataBufferByte (tempLargerData, tempExpectedSize);
                raster =
                Raster.createWritableRaster (sm, db, new Point (0, 0));
            } else {
                throw e;
            }
        }

        /* 
         * Workaround for a bug on the Mac -- a class cast exception in
         * drawImage() due to the wrong data buffer type (?)
         */
        BufferedImage bi = null;
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) cm;

            // choose the image type based on the size
            int type = BufferedImage.TYPE_BYTE_BINARY;
            if (getBitsPerComponent() == 8) {
                type = BufferedImage.TYPE_BYTE_INDEXED;
            }

            // create the image with an explicit indexed color model.
            bi = new BufferedImage(getWidth(), getHeight(), type, icm);

            // set the data explicitly as well
            bi.setData(raster);
        } else if (cm.getPixelSize() == 1 && cm.getNumComponents() == 1) {
        	//If the image is black and white only, convert it into BYTE_GRAY format
        	//This is a lot faster compared to just drawing the original image
        	
        	//Are pixels decoded?
        	int[] cc = new int[]{0, 1};
        	try {
        		PDFObject o = imageObj.getDictRef("Decode");
        		if (o != null && o.getAt(0) != null) {
        			cc[0] = o.getAt(0).getIntValue();
        			cc[1] = o.getAt(1).getIntValue();
        		}
			} catch (IOException e) {
			}
        	final byte[] ncc = new byte[]{(byte)-cc[0], (byte)-cc[1]};

        	bi = biColorToGrayscale(raster, ncc);
        	//Return when there is no SMask
        	if (getSMask() == null) return bi;
        }
        else {
            // Raster is already in a format which is supported by Java2D,
            // such as RGB or Gray.
            bi = new BufferedImage(cm, raster, true, null);
        }
        // hack to avoid *very* slow conversion
        ColorSpace cs = cm.getColorSpace();
        ColorSpace rgbCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        if (!isImageMask() && cs instanceof ICC_ColorSpace && !cs.equals(rgbCS)) {
            ColorConvertOp op = new ColorConvertOp(cs, rgbCS, null);
            
            BufferedImage converted = new BufferedImage(getWidth(),
                    getHeight(), BufferedImage.TYPE_INT_ARGB);

            bi = op.filter(bi, converted);
        }

        // add in the alpha data supplied by the SMask, if any
        PDFImage sMaskImage = getSMask();
        if (sMaskImage != null) {
            BufferedImage si = sMaskImage.getImage();
            BufferedImage outImage = new BufferedImage(getWidth(),
                    getHeight(), BufferedImage.TYPE_INT_ARGB);

            int[] srcArray = new int[this.width];
            int[] maskArray = new int[this.width];

            for (int i = 0; i < this.height; i++) {
                bi.getRGB(0, i, this.width, 1, srcArray, 0, this.width);
                si.getRGB(0, i, this.width, 1, maskArray, 0, this.width);

                for (int j = 0; j < this.width; j++) {
                    int ac = 0xff000000;

                    maskArray[j] = ((maskArray[j] & 0xff) << 24) | (srcArray[j] & ~ac);
                }

                outImage.setRGB(0, i, this.width, 1, maskArray, 0, this.width);
            }

            bi = outImage;
        }

        return (bi);
    }

	/**
	 * Creates a new image of type {@link TYPE_BYTE_GRAY} which represents
	 * the given raster
	 * @param raster Raster of an image with just two colors, bitwise encoded 
	 * @param ncc Array with two entries that describe the corresponding 
	 * 				gray values
	 */
    private BufferedImage biColorToGrayscale(final WritableRaster raster,
			final byte[] ncc) {
    	
		final byte[] bufferO = ((DataBufferByte) raster.getDataBuffer()).getData();
		
		BufferedImage converted = new BufferedImage(getWidth(),
				getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		byte[] buffer = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();        	

		int i = 0;
		final int height = converted.getHeight();
		final int width = converted.getWidth();
		for (int y = 0; y < height; y++) {
			int base = y*width + 7;
			if ((y+1)*width < buffer.length) {
				for (int x = 0; x < width; x += 8) {
					final byte bits = bufferO[i];
					i++;
					buffer[base - 7] = ncc[((bits >>> 7) & 1)];
					buffer[base - 6] = ncc[((bits >>> 6) & 1)];
					buffer[base - 5] = ncc[((bits >>> 5) & 1)];
					buffer[base - 4] = ncc[((bits >>> 4) & 1)];
					buffer[base - 3] = ncc[((bits >>> 3) & 1)];
					buffer[base - 2] = ncc[((bits >>> 2) & 1)];
					buffer[base - 1] = ncc[((bits >>> 1) & 1)];
					buffer[base] = ncc[(bits & 1)];
					
					/*for (byte j=7; j>=0; j--) {
						//final int c = (((bits & (1<<j)) >>> j));
						final int c = ((bits >>> j) & 1);
						buffer[base - j] = ncc[c];
					}*/
					base += 8;
				}        			
			}
			else {
				for (int x = 0; x < width; x += 8) {
					final byte bits = bufferO[i];
					i++;
					for (byte j=7; j>=0; j--) {
						if (base - j >= buffer.length) break;
						buffer[base - j] = ncc[((bits >>> j) & 1)];
					}
					base += 8; 			
				}
			}
		}
		return converted;
	}

    /**
     * Get the image's width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Set the image's width
     */
    protected void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get the image's height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Set the image's height
     */
    protected void setHeight(int height) {
        this.height = height;
    }

    /**
     * set the color key mask. It is an array of start/end entries
     * to indicate ranges of color indicies that should be masked out.
     * 
     * @param maskArrayObject
     */
    private void setColorKeyMask(PDFObject maskArrayObject) throws IOException {
        PDFObject[] maskObjects = maskArrayObject.getArray();
        this.colorKeyMask = null;
        int[] masks = new int[maskObjects.length];
        for (int i = 0; i < masks.length; i++) {
            masks[i] = maskObjects[i].getIntValue();
        }
        this.colorKeyMask = masks;
    }

    /**
     * Get the colorspace associated with this image, or null if there
     * isn't one
     */
    protected PDFColorSpace getColorSpace() {
        return this.colorSpace;
    }

    /**
     * Set the colorspace associated with this image
     */
    protected void setColorSpace(PDFColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }

    /**
     * Get the number of bits per component sample
     */
    protected int getBitsPerComponent() {
        return this.bpc;
    }

    /**
     * Set the number of bits per component sample
     */
    protected void setBitsPerComponent(int bpc) {
        this.bpc = bpc;
    }

    /**
     * Return whether or not this is an image mask
     */
    public boolean isImageMask() {
        return this.imageMask;
    }

    /**
     * Set whether or not this is an image mask
     */
    public void setImageMask(boolean imageMask) {
        this.imageMask = imageMask;
    }

    /** 
     * Return the soft mask associated with this image
     */
    public PDFImage getSMask() {
        return this.sMask;
    }

    /**
     * Set the soft mask image
     */
    protected void setSMask(PDFImage sMask) {
        this.sMask = sMask;
    }

    /**
     * Get the decode array
     */
    protected float[] getDecode() {
        return this.decode;
    }

    /**
     * Set the decode array
     */
    protected void setDecode(float[] decode) {
        this.decode = decode;
    }

    /**
     * get a Java ColorModel consistent with the current color space,
     * number of bits per component and decode array
     * 
     * @param bpc the number of bits per component
     */
    private ColorModel getColorModel() {
        PDFColorSpace cs = getColorSpace();

        if (cs instanceof IndexedColor) {
            IndexedColor ics = (IndexedColor) cs;

            byte[] components = ics.getColorComponents();
            int num = ics.getCount();

            // process the decode array
            if (this.decode != null) {
                byte[] normComps = new byte[components.length];

                // move the components array around
                for (int i = 0; i < num; i++) {
                    byte[] orig = new byte[1];
                    orig[0] = (byte) i;

                    float[] res = normalize(orig, null, 0);
                    int idx = (int) res[0];

                    normComps[i * 3] = components[idx * 3];
                    normComps[(i * 3) + 1] = components[(idx * 3) + 1];
                    normComps[(i * 3) + 2] = components[(idx * 3) + 2];
                }

                components = normComps;
            }

            // make sure the size of the components array is 2 ^ numBits
            // since if it's not, Java will complain
            int correctCount = 1 << getBitsPerComponent();
            if (correctCount < num) {
                byte[] fewerComps = new byte[correctCount * 3];

                System.arraycopy(components, 0, fewerComps, 0, correctCount * 3);

                components = fewerComps;
                num = correctCount;
            }
            if (this.colorKeyMask == null || this.colorKeyMask.length == 0) {
                return new IndexColorModel(getBitsPerComponent(), num, components,
                        0, false);
            } else {
                byte[] aComps = new byte[num * 4];
                int idx = 0;
                for (int i = 0; i < num; i++) {
                    aComps[idx++] = components[(i * 3)];
                    aComps[idx++] = components[(i * 3) + 1];
                    aComps[idx++] = components[(i * 3) + 2];
                    aComps[idx++] = (byte) 0xFF;
                }
                for (int i = 0; i < this.colorKeyMask.length; i += 2) {
                    for (int j = this.colorKeyMask[i]; j <= this.colorKeyMask[i + 1]; j++) {
                        aComps[(j * 4) + 3] = 0;    // make transparent
                    }
                }
                return new IndexColorModel(getBitsPerComponent(), num, aComps,
                        0, true);
            }
        } else if (cs instanceof AlternateColorSpace){
            ColorSpace altCS = new AltColorSpace(((AlternateColorSpace) cs).getFunktion(), cs.getColorSpace());
            int[] bits = new int[altCS.getNumComponents()];
            for (int i = 0; i <
                    bits.length; i++) {
                bits[i] = getBitsPerComponent();
            }
            return new DecodeComponentColorModel(altCS, bits);
        } else {
            // CMYK color space has been converted to RGB in DCTDecode
            if (cs.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
                ColorSpace rgbCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int[] bits = new int[rgbCS.getNumComponents()];
                for (int i = 0; i <
                        bits.length; i++) {
                    bits[i] = getBitsPerComponent();
                }
                return new DecodeComponentColorModel(rgbCS, bits);
            }
            int[] bits = new int[cs.getNumComponents()];
            for (int i = 0; i < bits.length; i++) {
                bits[i] = getBitsPerComponent();
            }

            return new DecodeComponentColorModel(cs.getColorSpace(), bits);
        }
    }

    /**
     * Normalize an array of values to match the decode array
     */
    private float[] normalize(byte[] pixels, float[] normComponents,
            int normOffset) {
        if (normComponents == null) {
            normComponents = new float[normOffset + pixels.length];
        }

        float[] decodeArray = getDecode();

        for (int i = 0; i < pixels.length; i++) {
            int val = pixels[i] & 0xff;
            int pow = ((int) Math.pow(2, getBitsPerComponent())) - 1;
            float ymin = decodeArray[i * 2];
            float ymax = decodeArray[(i * 2) + 1];

            normComponents[normOffset + i] =
                    FunctionType0.interpolate(val, 0, pow, ymin, ymax);
        }

        return normComponents;
    }

    /**
     * A wrapper for ComponentColorSpace which normalizes based on the 
     * decode array.
     */
    class DecodeComponentColorModel extends ComponentColorModel {

        public DecodeComponentColorModel(ColorSpace cs, int[] bpc) {
            super(cs, bpc, false, false, Transparency.OPAQUE,
                    DataBuffer.TYPE_BYTE);

            if (bpc != null) {
                this.pixel_bits = bpc.length * bpc[0];
            }
        }

        @Override
        public SampleModel createCompatibleSampleModel(int width, int height) {
            // workaround -- create a MultiPixelPackedSample models for 
            // single-sample, less than 8bpp color models
            if (getNumComponents() == 1 && getPixelSize() < 8) {
                return new MultiPixelPackedSampleModel(getTransferType(),
                        width,
                        height,
                        getPixelSize());
            }

            return super.createCompatibleSampleModel(width, height);
        }

        @Override
        public boolean isCompatibleRaster(Raster raster) {
            if (getNumComponents() == 1 && getPixelSize() < 8) {
                SampleModel sm = raster.getSampleModel();

                if (sm instanceof MultiPixelPackedSampleModel) {
                    return (sm.getSampleSize(0) == getPixelSize());
                } else {
                    return false;
                }
            }

            return super.isCompatibleRaster(raster);
        }

        @Override
        public float[] getNormalizedComponents(Object pixel,
                float[] normComponents, int normOffset) {
            if (getDecode() == null) {
                return super.getNormalizedComponents(pixel, normComponents,
                        normOffset);
            }

            return normalize((byte[]) pixel, normComponents, normOffset);
        }
    }
}