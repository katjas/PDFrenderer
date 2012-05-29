package com.sun.pdfview.annotation;

import java.io.IOException;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.annotation.PDFAnnotation.ANNOTATION_TYPE;

/**
 * PDF annotation describing a widget.
 * @since Aug 20, 2010
 */
public class WidgetAnnotation extends PDFAnnotation {

	private String fieldValue;
	private FieldType fieldType;
	private String fieldName;
	private PDFObject fieldValueRef;

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
	
}
