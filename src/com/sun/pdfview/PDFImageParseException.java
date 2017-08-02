package com.sun.pdfview;

import java.io.IOException;

/**
 * an exception class for recording errors when parsing an PDFImage
 * @author Katja Sondermann
 */
public class PDFImageParseException extends IOException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PDFImageParseException(String msg) {
        super(msg);
    }

    public PDFImageParseException(String msg, Throwable cause) {
        this(msg);
        initCause(cause);
    }
}
