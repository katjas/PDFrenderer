package org.loboevolution.pdfview.action;

import java.io.IOException;

import org.loboevolution.pdfview.PDFObject;

/**
 ***************************************************************************
 * URI action, containing a web link
 *
 * Author  Katja Sondermann
 * @since 07.07.2009
 ***************************************************************************
  *
 */
public class UriAction extends PDFAction {

	/** The URL this action links to */
	private final String uri;
	
	/**
	 ***********************************************************************
	 * Constructor, reading the URL from the given action object
	 *
	 * @param obj a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @param root a {@link org.loboevolution.pdfview.PDFObject} object.
	 * @throws java.io.IOException if any.
	 */
	public UriAction(PDFObject obj, PDFObject root) throws IOException {
		super("URI");
		this.uri = PdfObjectParseUtil.parseStringFromDict("URI", obj, true);
	}
	
	/**
	 ***********************************************************************
	 * Constructor
	 *
	 * @param uri a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public UriAction(String uri) throws IOException {
		super("URI");
		this.uri = uri;
	}

	/**
	 ***********************************************************************
	 * Get the URI this action directs to
	 *
	 * @return String
	 ***********************************************************************
	 */
	public String getUri() {
		return this.uri;
	}
}
