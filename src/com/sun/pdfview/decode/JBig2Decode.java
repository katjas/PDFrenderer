package com.sun.pdfview.decode;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jpedal.jbig2.JBIG2Decoder;
import org.jpedal.jbig2.JBIG2Exception;

import com.sun.pdfview.PDFObject;

/*****************************************************************************
 * Decoder for jbig2 images within PDFs.
 * Copied from
 * https://pdf-renderer.dev.java.net/issues/show_bug.cgi?id=67
 *
 *  Problem is also described in:
 *	http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4799898
 * @since 17.11.2010
 ***************************************************************************
 */
public class JBig2Decode {	
	protected static ByteBuffer decode(PDFObject dict, ByteBuffer buf,
			PDFObject params) throws IOException {

		byte[] bytes = new byte[buf.remaining()];
		buf.get(bytes, 0, bytes.length);

		return ByteBuffer.wrap(decode(dict, bytes));
	}


	protected static byte[] decode(PDFObject dict, byte[] source) throws IOException {
		JBIG2Decoder decoder;
		decoder = new JBIG2Decoder();
		try {
			byte[] globals = getOptionFieldBytes(dict, "JBIG2Globals");
			if (globals != null) {
				decoder.setGlobalData(globals);
			}
			decoder.decodeJBIG2(source);
		} catch (JBIG2Exception ex) {
			IOException ioException;

			ioException = new IOException();
			ioException.initCause(ex);
			throw ioException;
		}
		return decoder.getPageAsJBIG2Bitmap(0).getData(true);
	}


	public static byte[] getOptionFieldBytes(PDFObject dict, String name) throws IOException {

		PDFObject dictParams =  dict.getDictRef("DecodeParms");

		if (dictParams == null) {
			return null;
		}
		PDFObject value = dictParams.getDictRef(name);
		if (value == null) {
			return null;
		}
		return value.getStream();
	}

}