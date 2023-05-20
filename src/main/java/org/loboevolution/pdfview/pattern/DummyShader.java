package org.loboevolution.pdfview.pattern;

import java.awt.Color;
import java.io.IOException;

import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFPaint;

/**
 * <p>DummyShader class.</p>
 *
  *
  *
 */
public class DummyShader extends PDFShader {

	/**
	 * <p>Constructor for DummyShader.</p>
	 *
	 * @param type a int.
	 */
	protected DummyShader(int type) {
		super(type);
	}

	/** {@inheritDoc} */
	@Override
	public void parse(PDFObject shareObj) throws IOException {
		
	}

	/** {@inheritDoc} */
	@Override
	public PDFPaint getPaint() {
		return PDFPaint.getPaint(Color.PINK);
	}
		
}
