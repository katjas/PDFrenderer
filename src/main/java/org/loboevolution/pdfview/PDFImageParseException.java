package org.loboevolution.pdfview;

import java.io.IOException;

/**
 * an exception class for recording errors when parsing an PDFImage
 *
 * Author Katja Sondermann
  *
 */
public class PDFImageParseException extends IOException {
    /**
     * <p>Constructor for PDFImageParseException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public PDFImageParseException(String msg) {
        super(msg);
    }

    /**
     * <p>Constructor for PDFImageParseException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public PDFImageParseException(String msg, Throwable cause) {
        this(msg);
        initCause(cause);
    }
}
