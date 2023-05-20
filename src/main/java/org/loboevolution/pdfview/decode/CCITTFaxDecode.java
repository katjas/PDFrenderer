package org.loboevolution.pdfview.decode;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jpedal.io.filter.ccitt.CCITT1D;
import org.jpedal.io.filter.ccitt.CCITT2D;
import org.jpedal.io.filter.ccitt.CCITTDecoder;
import org.jpedal.io.filter.ccitt.CCITTMix;
import org.loboevolution.pdfview.PDFObject;

/**
 * <p>CCITTFaxDecode class.</p>
 */
public class CCITTFaxDecode {

	/**
	 * <p>decode.</p>
	 *
	 * @param dict a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param buf a {@link java.nio.ByteBuffer} object.
	 * @param params a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @return a {@link java.nio.ByteBuffer} object.
	 * @throws java.io.IOException if any.
	 */
	protected static ByteBuffer decode(PDFObject dict, ByteBuffer buf,
            PDFObject params) throws IOException {

		byte[] bytes = new byte[buf.remaining()];
	    buf.get(bytes, 0, bytes.length);
		return ByteBuffer.wrap(decode(dict, bytes));
	}


	/**
	 * <p>decode.</p>
	 *
	 * @param dict a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param source an array of {@link byte} objects.
	 * @return an array of {@link byte} objects.
	 * @throws java.io.IOException if any.
	 */
	protected static byte[] decode(PDFObject dict, byte[] source) throws IOException {
		int width = 1728;
		PDFObject widthDef = dict.getDictRef("Width");
		if (widthDef == null) {
			widthDef = dict.getDictRef("W");
		}
		if (widthDef != null) {
			width = widthDef.getIntValue();
		}
		int height = 0;
		PDFObject heightDef = dict.getDictRef("Height");
		if (heightDef == null) {
			heightDef = dict.getDictRef("H");
		}
		if (heightDef != null) {
			height = heightDef.getIntValue();
		}

		int columns = getOptionFieldInt(dict, "Columns", width);
		int rows = getOptionFieldInt(dict, "Rows", height);
		int k = getOptionFieldInt(dict, "K", 0);
		boolean align = getOptionFieldBoolean(dict, "EncodedByteAlign", false);
		boolean blackIsOne = getOptionFieldBoolean(dict, "BlackIs1", false);

		CCITTDecoder decoder;
		if (k == 0){
			// Pure 1D decoding, group3
			decoder = new CCITT1D(source, columns, rows, blackIsOne, align);
		} else if (k < 0) {
			// Pure 2D, group 4
			decoder = new CCITT2D(source, columns, rows, blackIsOne, align);
		} else /*if (k > 0)*/ {
			// Mixed 1/2 D encoding we can use either for maximum compression
			// A 1D line can be followed by up to K-1 2D lines
			decoder = new CCITTMix(source, columns, rows, blackIsOne, align);
		}

		byte[] result;
		try {
			result = decoder.decode();
		} catch (RuntimeException e) {
			System.out.println("Error decoding CCITTFax image k: "+ k);
			if (k >= 0) {
				result = new CCITT2D(source, columns, rows, blackIsOne, align).decode();
			} else {
				throw e;
			}
		}

		return result;
	}

	/**
	 * <p>getOptionFieldInt.</p>
	 *
	 * @param dict a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param name a {@link java.lang.String} object.
	 * @param defaultValue a int.
	 * @return a int.
	 * @throws java.io.IOException if any.
	 */
	public static int getOptionFieldInt(PDFObject dict, String name, int defaultValue) throws IOException {

        PDFObject dictParams = getDecodeParams(dict);

		if (dictParams == null) {
			return defaultValue;
		}
		PDFObject value = dictParams.getDictRef(name);
		if (value == null) {
			return defaultValue;
		}
		return value.getIntValue();
	}

	/**
	 * <p>getOptionFieldBoolean.</p>
	 *
	 * @param dict a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param name a {@link java.lang.String} object.
	 * @param defaultValue a boolean.
	 * @return a boolean.
	 * @throws java.io.IOException if any.
	 */
	public static boolean getOptionFieldBoolean(PDFObject dict, String name, boolean defaultValue) throws IOException {

        PDFObject dictParams = getDecodeParams(dict);

		if (dictParams == null) {
			return defaultValue;
		}
		PDFObject value = dictParams.getDictRef(name);
		if (value == null) {
			return defaultValue;
		}
		return value.getBooleanValue();
	}

    private static PDFObject getDecodeParams(PDFObject dict) throws IOException {
        PDFObject decdParams = dict.getDictRef("DecodeParms");
        if (decdParams != null && decdParams.getType() == PDFObject.ARRAY) {
            return decdParams.getArray()[0];
        }
        return decdParams;
    }
}
