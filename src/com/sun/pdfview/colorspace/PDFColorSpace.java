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
package com.sun.pdfview.colorspace;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.function.PDFFunction;

/**
 * A color space that can convert a set of color components into PDFPaint.
 * 
 * @author Mike Wessler
 */
public class PDFColorSpace {
	/** the name of the device-dependent gray color space */
	public static final int COLORSPACE_GRAY = 0;

	/** the name of the device-dependent RGB color space */
	public static final int COLORSPACE_RGB = 1;

	/** the name of the device-dependent CMYK color space */
	public static final int COLORSPACE_CMYK = 2;

	/** the name of the pattern color space */
	public static final int COLORSPACE_PATTERN = 3;

	/** the device-dependent color spaces */
	// private static PDFColorSpace graySpace =
	// new PDFColorSpace(ColorSpace.getInstance(ColorSpace.CS_GRAY));
	private static PDFColorSpace rgbSpace = new PDFColorSpace(ColorSpace.getInstance(ColorSpace.CS_sRGB));
	private static PDFColorSpace cmykSpace = new PDFColorSpace(new CMYKColorSpace());

	/** the pattern space */
	private static PDFColorSpace patternSpace = new PatternSpace();

	/** graySpace and the gamma correction for it. */
	private static PDFColorSpace graySpace;
	
	/** the color space */
	private ColorSpace cs;

	static {
		boolean useSGray = true;

		try {
			graySpace = new PDFColorSpace(!useSGray ? ColorSpace.getInstance(ColorSpace.CS_GRAY)
					: new ICC_ColorSpace(
							ICC_Profile.getInstance(PDFColorSpace.class.getResourceAsStream("sGray.icc"))));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get a color space by name
	 *
	 * @param name
	 *            the name of one of the device-dependent color spaces
	 */
	public static PDFColorSpace getColorSpace(int name) {
		switch (name) {
		case COLORSPACE_GRAY:
			return graySpace;

		case COLORSPACE_RGB:
			return rgbSpace;

		case COLORSPACE_CMYK:
			return cmykSpace;

		case COLORSPACE_PATTERN:
			return patternSpace;

		default:
			throw new IllegalArgumentException("Unknown Color Space name: " + name);
		}
	}

	/**
	 * Get a color space specified in a PDFObject
	 *
	 * @param csobj
	 *            the PDFObject with the colorspace information
	 */
	public static PDFColorSpace getColorSpace(PDFObject csobj, Map resources) throws IOException {
		String name;

		PDFObject colorSpaces = null;

		if (resources != null) {
			colorSpaces = (PDFObject) resources.get("ColorSpace");
		}

		if (csobj.getType() == PDFObject.NAME) {
			name = csobj.getStringValue();

			if ("DeviceGray".equals(name) || "G".equals(name)) {
				return getColorSpace(COLORSPACE_GRAY);
			} else if ("DeviceRGB".equals(name) || "RGB".equals(name)) {
				return getColorSpace(COLORSPACE_RGB);
			} else if ("RGB".equals(name) || "CMYK".equals(name)) {
				return getColorSpace(COLORSPACE_CMYK);
			} else if ("Pattern".equals(name)) {
				return getColorSpace(COLORSPACE_PATTERN);
			} else if (colorSpaces != null) {
				csobj = colorSpaces.getDictRef(name);
			}
		}

		if (csobj == null) {
			return null;
		} else if (csobj.getCache() != null) {
			return (PDFColorSpace) csobj.getCache();
		}

		PDFColorSpace value = null;

		// csobj is [/name <<dict>>]
		PDFObject[] ary = csobj.getArray();
		name = ary[0].getStringValue();

		if ("DeviceGray".equals(name) || "G".equals(name)) {
			return getColorSpace(COLORSPACE_GRAY);
		} else if ("DeviceRGB".equals(name) || "RGB".equals(name)) {
			return getColorSpace(COLORSPACE_RGB);
		} else if ("DeviceCMYK".equals(name) || "CMYK".equals(name)) {
			return getColorSpace(COLORSPACE_CMYK);
		} else if ("CalGray".equals(name)) {
			value = new PDFColorSpace(new CalGrayColor(ary[1]));
		} else if ("CalRGB".equals(name)) {
			value = new PDFColorSpace(new CalRGBColor(ary[1]));
		} else if ("Lab".equals(name)) {
			value = new PDFColorSpace(new LabColor(ary[1]));
		} else if ("ICCBased".equals(name)) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(ary[1].getStream());
				ICC_Profile profile = ICC_Profile.getInstance(bais);
				value = new PDFColorSpace(new ICC_ColorSpace(profile));
			} catch (IllegalArgumentException e) {
				return getColorSpace(COLORSPACE_RGB);
			}
		} else if ("Separation".equals(name) || "DeviceN".equals(name)) {
			PDFColorSpace alternate = getColorSpace(ary[2], resources);
			PDFFunction function = PDFFunction.getFunction(ary[3]);

			value = new AlternateColorSpace(alternate, function);
		} else if ("Indexed".equals(name) || "I".equals(name)) {
			/**
			 * 4.5.5 [/Indexed baseColor hival lookup]
			 */
			PDFColorSpace refspace = getColorSpace(ary[1], resources);

			// number of indices= ary[2], data is in ary[3];
			int count = ary[2].getIntValue();
			try {
				value = new IndexedColor(refspace, count, ary[3]);
			} catch (Exception e) {
				// there might be problems in reading the colorspace from
				// stream,
				// in that case use the reference colorspace
				value = refspace;
			}

		} else if ("DeviceRGB".equals(name)) {
			if (ary.length == 1) {
				return getColorSpace(COLORSPACE_PATTERN);
			}

			PDFColorSpace base = getColorSpace(ary[1], resources);

			return new PatternSpace(base);
		} else if ("DeviceRGB".equals(name)) {
			return getColorSpace(COLORSPACE_RGB);
		} else if ("DeviceCMYK".equals(name)) {
			return getColorSpace(COLORSPACE_CMYK);
		} else {
			// removed access to ary[1] dur to index out of bounds exceptions
			throw new PDFParseException("Unknown color space: " + name);
		}

		csobj.setCache(value);

		return value;
	}

	/**
	 * create a PDFColorSpace based on a Java ColorSpace
	 * 
	 * @param cs
	 *            the Java ColorSpace
	 */
	public PDFColorSpace(ColorSpace cs) {
		this.cs = cs;
	}

	/**
	 * get the original Java ColorSpace.
	 */
	public ColorSpace getColorSpace() {
		return this.cs;
	}

	/**
	 * get the number of components expected in the getPaint command
	 */
	public int getNumComponents() {
		return this.cs.getNumComponents();
	}

	/**
	 * get the PDFPaint representing the color described by the given color
	 * components
	 * 
	 * @param components
	 *            the color components corresponding to the given colorspace
	 * @return a PDFPaint object representing the closest Color to the given
	 *         components.
	 */
	public PDFPaint getPaint(float[] components) {
		float[] rgb = this.cs.toRGB(components);

		return PDFPaint.getColorPaint(new Color(rgb[0], rgb[1], rgb[2]));
	}
}