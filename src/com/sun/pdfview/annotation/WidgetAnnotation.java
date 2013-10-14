package com.sun.pdfview.annotation;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sun.pdfview.PDFCmd;
import com.sun.pdfview.PDFImage;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.PDFParser;

/**
 * PDF annotation describing a widget.
 * @since Aug 20, 2010
 */
public class WidgetAnnotation extends PDFAnnotation {

	private String fieldValue;
	private FieldType fieldType;
	private String fieldName;
	private PDFObject fieldValueRef;
	private List<PDFCmd> cmd;

	/**
	 * Type for PDF form elements
	 * @version $Id: WidgetAnnotation.java,v 1.2 2010-09-30 10:34:44 xphc Exp $ 
	 * @author  xphc
	 * @since Aug 20, 2010
	 */
	public enum FieldType {
		/** Button Field */
		Button("Btn"),
		/** Text Field */
		Text("Tx"),
		/** Choice Field */
		Choice("Ch"),
		/** Signature Field */
		Signature("Sig");
		
		private final String typeCode;

		FieldType(String typeCode) {
			this.typeCode = typeCode;
		}
		
		static FieldType getByCode(String typeCode) {
			FieldType[] values = values();
			for (FieldType value : values) {
				if (value.typeCode.equals(typeCode))
					return value;
			}
			return null;
		}
	}

	public WidgetAnnotation(PDFObject annotObject) throws IOException {
		super(annotObject, ANNOTATION_TYPE.WIDGET);
		
		// The type of field that this dictionary describes. Field type is
		// present for terminal fields but is inherited from parent if absent
		// (see PDF Reference 1.7 table 8.69)
		PDFObject fieldTypeRef = annotObject.getDictRef("FT");
		if (fieldTypeRef != null) {
			// terminal field
			this.fieldType = FieldType.getByCode(fieldTypeRef.getStringValue());
		}
		else {
			// must check parent since field type is inherited
			PDFObject parent = annotObject.getDictRef("Parent");
			while (parent != null && parent.isIndirect()) {
				parent = parent.dereference();
			}
			if (parent != null) {
				fieldTypeRef = parent.getDictRef("FT");
				this.fieldType = FieldType.getByCode(fieldTypeRef.getStringValue());
			}
		}
		
		// Name defined for the field
		PDFObject fieldNameRef = annotObject.getDictRef("T");
		if (fieldNameRef != null) {
			this.fieldName = fieldNameRef.getTextStringValue();
		}
		this.fieldValueRef = annotObject.getDictRef("V");
		if (this.fieldValueRef != null) {
			this.fieldValue = this.fieldValueRef.getTextStringValue();
		}
		parseAP(annotObject.getDictRef("AP"));
	}
	
	private void parseAP(PDFObject dictRef) throws IOException {
		if(dictRef == null) {
			return;
		}
		PDFObject normalAP = dictRef.getDictRef("N");
		if(normalAP == null) {
			return;
		}
		cmd = parseCommand(normalAP);
	}
	
	private List<PDFCmd> parseCommand(PDFObject obj) throws IOException {
        String type = obj.getDictRef("Subtype").getStringValue();
        if (type == null) {
            type = obj.getDictRef ("S").getStringValue ();
        }
        ArrayList<PDFCmd> result = new ArrayList<PDFCmd>();
        result.add(PDFPage.createPushCmd());
        result.add(PDFPage.createPushCmd());
        if (type.equals("Image")) {
            // stamp annotation transformation
            AffineTransform rectAt = getPositionTransformation();
            result.add(PDFPage.createXFormCmd(rectAt));
            
        	PDFImage img = PDFImage.createImage(obj, new HashMap<String, PDFObject>() , false);        	
        	result.add(PDFPage.createImageCmd(img));
        } else if (type.equals("Form")) {
        	
            // rats.  parse it.
            PDFObject bobj = obj.getDictRef("BBox");
            Float bbox = new Rectangle2D.Float(bobj.getAt(0).getFloatValue(),
                    bobj.getAt(1).getFloatValue(),
                    bobj.getAt(2).getFloatValue(),
                    bobj.getAt(3).getFloatValue());
            PDFPage formCmds = new PDFPage(bbox, 0);
            // stamp annotation transformation
            AffineTransform rectAt = getPositionTransformation();
            formCmds.addXform(rectAt);

            // form transformation
            AffineTransform at;
            PDFObject matrix = obj.getDictRef("Matrix");
            if (matrix == null) {
                at = new AffineTransform();
            } else {
                float elts[] = new float[6];
                for (int i = 0; i < elts.length; i++) {
                    elts[i] = (matrix.getAt(i)).getFloatValue();
                }
                at = new AffineTransform(elts);
            }
            formCmds.addXform(at);
            
            HashMap<String,PDFObject> r = new HashMap<String,PDFObject>(new HashMap<String, PDFObject>());
            PDFObject rsrc = obj.getDictRef("Resources");
            if (rsrc != null) {
                r.putAll(rsrc.getDictionary());
            }

            PDFParser form = new PDFParser(formCmds, obj.getStream(), r);
            form.go(true);

            result.addAll(formCmds.getCommands());
        } else {
            throw new PDFParseException("Unknown XObject subtype: " + type);
        }
        result.add(PDFPage.createPopCmd());
        result.add(PDFPage.createPopCmd());
        return result;
	}
	
	/**
	 * Transform to the position of the stamp annotation
	 * @return
	 */
	private AffineTransform getPositionTransformation() {
		Float rect2 = getRect();
		double[] f = new double[] {1,
				0,
				0,
				1,
				rect2.getMinX(),
				rect2.getMinY()};
		return new AffineTransform(f);
	}

	/**
	 * Returns the type of the field
	 * @return Field type
	 */
	public FieldType getFieldType() {
		return this.fieldType;
	}
	
	/**
	 * The field's value as a string. Might be {@code null}.
	 * @return The field value or {@code null}.
	 */
	public String getFieldValue() {
		return this.fieldValue;
	}

	/**
	 * Sets the field value for a text field. Note: this doesn't actually change
	 * the PDF file yet.
	 * 
	 * @param fieldValue
	 *            The new value for the text field
	 */
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	/**
	 * Name for this widget.
	 * @return Widget name
	 */
	public String getFieldName() {
		return this.fieldName;
	}
	
	@Override
	public List<PDFCmd> getPageCommandsForAnnotation() {
		List<PDFCmd> pageCommandsForAnnotation = super.getPageCommandsForAnnotation();
		// cmd might be null if there is no AP (appearance dictionary) 
		// AP is optional see PDF Reference 1.7 table 8.15
		if (this.cmd != null) {
			pageCommandsForAnnotation.addAll(this.cmd);
		}
		return pageCommandsForAnnotation;
	}
	
}
