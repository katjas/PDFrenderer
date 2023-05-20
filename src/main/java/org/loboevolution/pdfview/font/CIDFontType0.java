package org.loboevolution.pdfview.font;

import java.io.IOException;

import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.font.cid.PDFCMap;
import org.loboevolution.pdfview.font.ttf.AdobeGlyphList;

/**
 ***************************************************************************
 * At the moment this is not fully supported to parse CID based fonts
 * As a hack we try to use a built in font as substitution and use a
 * toUnicode map to translate the characters if available.
 *
 * @version $Id: CIDFontType0.java,v 1.1 2011-08-03 15:48:56 bros Exp $
 * Author  Bernd Rosstauscher
 * @since 03.08.2011
 ***************************************************************************
 */
public class CIDFontType0 extends BuiltinFont {

	private PDFCMap glyphLookupMap;

	/**
	 ***********************************************************************
	 * Constructor
	 *
	 * @param baseFont a {@link java.lang.String} object.
	 * @param fontObj a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param descriptor a {@link org.loboevolution.pdfview.font.PDFFontDescriptor} object.
	 * @throws java.io.IOException if any.
	 */
	public CIDFontType0(String baseFont, PDFObject fontObj,
			PDFFontDescriptor descriptor) throws IOException {
		super(baseFont, fontObj, descriptor);
	}
	
	/**
	 * <p>parseToUnicodeMap.</p>
	 *
	 * @param fontObj a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @throws java.io.IOException if any.
	 */
	public void parseToUnicodeMap(PDFObject fontObj) throws IOException {
		PDFObject toUnicode = fontObj.getDictRef("ToUnicode");
		if (toUnicode != null) {
			PDFCMap cmap = PDFCMap.getCMap(toUnicode);
			this.glyphLookupMap = cmap;
		}
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Get a character from the first font in the descendant fonts array
	 */
    @Override
	protected PDFGlyph getGlyph(char src, String name) {
        //TODO BROS 03.08.2011 Hack for unsupported Type0 CID based fonts
		// If we have a toUnicodeMap then try to use that one when mapping to our build in font.
    	// See "9.10 Extraction of Text Content" in the PDF spec.
        if (this.glyphLookupMap != null) {
        	src = this.glyphLookupMap.map(src);
            //The preferred method of getting the glyph should be by name. 
            if (name == null && src != 160) {//unless it NBSP
            	//so, try to find the name by the char
            	name = AdobeGlyphList.getGlyphName(src);
            }
        }
		return super.getGlyph(src, name);
    }
}
