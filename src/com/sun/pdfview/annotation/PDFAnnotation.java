package com.sun.pdfview.annotation;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.sun.pdfview.PDFCmd;
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
	public enum ANNOTATION_TYPE{
		UNKNOWN("-", 0, PDFAnnotation.class),
		LINK("Link", 1, LinkAnnotation.class),
		WIDGET("Widget", 2, WidgetAnnotation.class),
		STAMP("Stamp", 3, StampAnnotation.class),
		FREETEXT("FreeText", 5, FreetextAnnotation.class),
		// TODO 28.03.2012: add more annotation types
		;
		
		private String definition; 
		private int internalId;
		private Class<?> className;
		private ANNOTATION_TYPE(String definition, int typeId, Class<?> className) {
			this.definition = definition;
			this.internalId = typeId;
			this.className = className;
		}
		/**
		 * @return the definition
		 */
		public String getDefinition() {
			return definition;
		}
		/**
		 * @return the internalId
		 */
		public int getInternalId() {
			return internalId;
		}
		
		/**
		 * @return the className
		 */
		public Class<?> getClassName() {
			return className;
		}
		
		/**
		 * Get annotation type by it's type 
		 * @param definition
		 * @return
		 */
		public static ANNOTATION_TYPE getByDefinition(String definition) {
			for (ANNOTATION_TYPE type : values()) {
				if(type.definition.equals(definition)) {
					return type;
				}
			}
			return UNKNOWN;
		}		
	}
	
	/** Definition of some annotation sub-types*/
	public static final String GOTO = "GoTo";
	public static final String GOTOE = "GoToE";
	public static final String GOTOR = "GoToR";
	public static final String URI = "URI";
	
	private final PDFObject pdfObj;
	private final ANNOTATION_TYPE type;
	private final Float rect;

	/*************************************************************************
	 * Constructor
	 * @param annotObject - the PDFObject which contains the annotation description
	 * @throws IOException 
	 ************************************************************************/
	public PDFAnnotation(PDFObject annotObject) throws IOException{
		this(annotObject, ANNOTATION_TYPE.UNKNOWN);
	}

	/*************************************************************************
	 * Constructor
	 * @param annotObject - the PDFObject which contains the annotation description
	 * @throws IOException 
	 ************************************************************************/
	protected PDFAnnotation(PDFObject annotObject, ANNOTATION_TYPE type) throws IOException{
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
		if(subtypeValue == null) {
			return null;
		}			
		String subtypeS = subtypeValue.getStringValue();
		
		ANNOTATION_TYPE annotationType = ANNOTATION_TYPE.getByDefinition(subtypeS);
		Class<?> className = annotationType.getClassName();
		
		Constructor<?> constructor;
		try {
			constructor = className.getConstructor(PDFObject.class);
			return (PDFAnnotation)constructor.newInstance(parent);
		} catch (Exception e) {
			throw new PDFParseException("Could not parse annotation!", e);
		} 
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
	public ANNOTATION_TYPE getType() {
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
	
	/**
	 * Get list of pdf commands for this annotation
	 * @return 
	 */
	public List<PDFCmd> getPageCommandsForAnnotation() {
		return new ArrayList<PDFCmd>();
	}
}
