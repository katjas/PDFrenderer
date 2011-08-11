package com.sun.pdfview.annotation;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/*****************************************************************************
 * Encapsulate a PDF annotation. This is only the super-class of PDF annotations, 
 * which has an "unknown" annotation type. 
 * Use the createAnnotation() method for getting an annotation of the correct 
 * type (if implemented).
 *
 * @author  Katja Sondermann
 * @since 03.07.2009
 ****************************************************************************/
public class PDFAnnotation{
	/** Definition of some annotation types*/
	public static final String LINK = "Link";
	public static final String WIDGET = "Widget";
	
	/** Definition of some annotation sub-types*/
	public static final String GOTO = "GoTo";
	public static final String GOTOE = "GoToE";
	public static final String GOTOR = "GoToR";
	public static final String URI = "URI";

	/** 
	 * The supported annotation types, used as internal 
	 * types and for getting the annotations via PDFPage
	 **/
	public static final int UINKNOWN_ANNOTATION = 0;
	public static final int LINK_ANNOTATION = 1;
	public static final int WIDGET_ANNOTATION = 2;
	
	private PDFObject pdfObj;
	private int type;
	private Float rect;

	/*************************************************************************
	 * Constructor
	 * @param annotObject - the PDFObject which contains the annotation description
	 * @throws IOException 
	 ************************************************************************/
	protected PDFAnnotation(PDFObject annotObject, int type) throws IOException{
		this.pdfObj = annotObject;
		// in case a general "PdfAnnotation" is created the type is unknown
		this.type = type;
		this.rect = this.parseRect(annotObject.getDictRef("Rect"));
	}

	/*************************************************************************
	 * Create a new PDF annotation object.
	 * 
	 * Currently supported annotation types:
	 * <li>Link annotation</li>
	 * 
	 * @param parent
	 * @return PDFAnnotation
	 * @throws IOException 
	 ************************************************************************/
	public static PDFAnnotation createAnnotation(PDFObject parent) throws IOException{
		PDFObject subtypeValue = parent.getDictRef("Subtype");
		String subtypeS = subtypeValue.getStringValue();
		if(LINK.equals(subtypeS)){
			return new LinkAnnotation(parent);
		}
		if (WIDGET.equals(subtypeS)) {
			return new WidgetAnnotation(parent);
		}
		// TODO implement other annotation types?
		return new PDFAnnotation(parent, UINKNOWN_ANNOTATION);
	}

    /**
     * Get a Rectangle2D.Float representation for a PDFObject that is an
     * array of four Numbers.
     * @param obj a PDFObject that represents an Array of exactly four
     * Numbers.
     */
    public Rectangle2D.Float parseRect(PDFObject obj) throws IOException {
        if (obj.getType() == PDFObject.ARRAY) {
            PDFObject bounds[] = obj.getArray();
            if (bounds.length == 4) {
                return new Rectangle2D.Float(bounds[0].getFloatValue(),
                        bounds[1].getFloatValue(),
                        bounds[2].getFloatValue() - bounds[0].getFloatValue(),
                        bounds[3].getFloatValue() - bounds[1].getFloatValue());
            } else {
                throw new PDFParseException("Rectangle definition didn't have 4 elements");
            }
        } else {
            throw new PDFParseException("Rectangle definition not an array");
        }
    }

    /*************************************************************************
     * Get the PDF Object which contains the annotation values
     * @return PDFObject
     ************************************************************************/
	public PDFObject getPdfObj() {
		return this.pdfObj;
	}

	/*************************************************************************
	 * Get the annotation type
	 * @return int
	 ************************************************************************/
	public int getType() {
		return this.type;
	}

	/*************************************************************************
	 * Get the rectangle on which the annotation should be applied to
	 * @return Rectangle2D.Float	
	 ************************************************************************/
	public Float getRect() {
		return this.rect;
	}

	@Override
	public String toString() {
		return this.pdfObj.toString();
	}
}
