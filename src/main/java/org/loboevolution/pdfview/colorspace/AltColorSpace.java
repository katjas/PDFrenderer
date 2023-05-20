package org.loboevolution.pdfview.colorspace;

import java.awt.color.ColorSpace;

import org.loboevolution.pdfview.function.PDFFunction;

/**
 ***************************************************************************
 * Color Space implementation for handling the PDF AlternateColorSpace.
 * A PDF function is applied to colorvalues before converting.
 *
 * Author  Katja Sondermann
 * @since 06.01.2011
 ***************************************************************************
  *
 */
public class AltColorSpace extends ColorSpace {

	private final PDFFunction fkt;
	private final ColorSpace origCs;
	/**
	 * Create a new CMYKColorSpace Instance.
	 *
	 * @param fkt a {@link org.loboevolution.pdfview.function.PDFFunction} object.
	 * @param origCs a {@link java.awt.color.ColorSpace} object.
	 */
	public AltColorSpace(PDFFunction fkt, ColorSpace origCs) {
		super(origCs.getType(), fkt.getNumInputs());
		this.fkt = fkt;
		this.origCs = origCs;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Converts from CIEXYZ.
	 * @see java.awt.color.ColorSpace#fromCIEXYZ(float[])
	 */
	@Override
	public float[] fromCIEXYZ(float[] p_colorvalue) {
		p_colorvalue = this.fkt.calculate(p_colorvalue);
		return this.origCs.fromCIEXYZ(p_colorvalue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Converts a given RGB.
	 * @see java.awt.color.ColorSpace#fromRGB(float[])
	 */
	@Override
	public float[] fromRGB(float[] p_rgbvalue) {
		p_rgbvalue = this.fkt.calculate(p_rgbvalue);
		return this.origCs.fromCIEXYZ(p_rgbvalue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Converts to CIEXYZ.
	 * @see java.awt.color.ColorSpace#toCIEXYZ(float[])
	 */
	@Override
	public float[] toCIEXYZ(float[] p_colorvalue) {
		float[] colorvalue = this.fkt.calculate(p_colorvalue);
		return this.origCs.toCIEXYZ(colorvalue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Converts to RGB.
	 * @see java.awt.color.ColorSpace#toRGB(float[])
	 */
	@Override
	public float[] toRGB(float[] p_colorvalue) {
		float[] colorvalue = this.fkt.calculate(p_colorvalue);
		return this.origCs.toRGB(colorvalue);
	}
}
