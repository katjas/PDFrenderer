package org.loboevolution.pdfview.action;

import java.io.IOException;

import org.loboevolution.pdfview.PDFDestination;
import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFParseException;

/**
 ***************************************************************************
 * Utility class for parsing values from a PDFObject
 *
 * Author  Katja Sondermann
 * @since 08.07.2009
 ***************************************************************************
  *
 */
public class PdfObjectParseUtil {
	
	/**
	 ***********************************************************************
	 * Parse a String value with the given key from parent object. If it's mandatory
	 * and not available, an exception will be thrown.
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param parent a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param mandatory a boolean.
	 * @return String - can be <code>null</code> if not mandatory
	 * @throws java.io.IOException if any.
	 */
	public static String parseStringFromDict(String key, PDFObject parent, boolean mandatory) throws IOException{
		PDFObject val = parent;
		while (val.getType() == PDFObject.DICTIONARY) {
			val = val.getDictRef(key);
			if (val == null) {
				if (mandatory) {
					throw new PDFParseException(key + "value could not be parsed : " + parent.toString());	
				}
				return null;
			}
		}
		return val.getStringValue();
	}

	/**
	 ***********************************************************************
	 * Parse a Boolean value with the given key from parent object. If it's mandatory
	 * and not available, an exception will be thrown.
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param parent a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param mandatory a boolean.
	 * @return boolean - <code>false</code> if not available and not mandatory
	 * @throws java.io.IOException if any.
	 */
	public static boolean parseBooleanFromDict(String key, PDFObject parent, boolean mandatory) throws IOException{
		PDFObject val = parent.getDictRef(key);
		if (val == null) {
			if (mandatory) {
				throw new PDFParseException(key + "value could not be parsed : " + parent.toString());	
			}
			return false;
		}
		return val.getBooleanValue();
	}
	
	/**
	 ***********************************************************************
	 * Parse a integer value with the given key from parent object. If it's mandatory
	 * and not available, an exception will be thrown.
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param parent a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param mandatory a boolean.
	 * @return int - returns "0" in case the value is not a number
	 * @throws java.io.IOException if any.
	 */
	public static int parseIntegerFromDict(String key, PDFObject parent, boolean mandatory) throws IOException{
		PDFObject val = parent.getDictRef(key);
		if (val == null) {
			if (mandatory) {
				throw new PDFParseException(key + "value could not be parsed : " + parent.toString());	
			}
			return 0;
		}
		return val.getIntValue();
	}
	
	/**
	 ***********************************************************************
	 * Parse a destination object
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param parent a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param root a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param mandatory a boolean.
	 * @return PDFDestination  - can be <code>null</code> if not mandatory
	 * @throws java.io.IOException if any.
	 */
	public static PDFDestination parseDestination(String key, PDFObject parent, PDFObject root, boolean mandatory) throws IOException{
		PDFObject destObj = parent.getDictRef(key);
		if (destObj == null) {
			if (mandatory) {
				throw new PDFParseException("Error parsing destination " + parent);
			}
			return null;
		}
		return PDFDestination.getDestination(destObj, root);

	}
}
