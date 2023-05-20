package org.loboevolution.pdfview.annotation;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.loboevolution.pdfview.PDFCmd;
import org.loboevolution.pdfview.PDFImage;
import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFPage;
import org.loboevolution.pdfview.PDFParseException;
import org.loboevolution.pdfview.PDFParser;

/**
 ***************************************************************************
 * PDF annotation describing a stamp
 *
 * Author Katja Sondermann
 * @since 26.03.2012
 ***************************************************************************
  *
 */
public class StampAnnotation extends PDFAnnotation {
	private String iconName;
	private PDFAnnotation popupAnnotation;
	private PDFObject onAppearance;
	private PDFObject offAppearance;
	private List<PDFCmd> onCmd;
	private List<PDFCmd> offCmd;
	private boolean appearanceStateOn;
	
	/**
	 ***********************************************************************
	 * Constructor
	 *
	 * @param annotObject a {@link PDFObject} object.
	 * @param type a ANNOTATION_TYPE object.
	 * @throws IOException if any.
	 */
	public StampAnnotation(PDFObject annotObject, ANNOTATION_TYPE type) throws IOException {
		super(annotObject, type);
		
		parsePopupAnnotation(annotObject.getDictRef("Popup"));
		
		parseAP(annotObject.getDictRef("AP"));			
	}

	/**
	 ***********************************************************************
	 * Constructor
	 *
	 * @param annotObject a {@link PDFObject} object.
	 * @throws IOException if any.
	 */
	public StampAnnotation(PDFObject annotObject) throws IOException {
		this(annotObject, ANNOTATION_TYPE.STAMP);
	}
	
	private void parseAP(PDFObject dictRef) throws IOException {
		if (dictRef == null) {
			return;
		}
		PDFObject normalAP = dictRef.getDictRef("N");
		if (normalAP == null) {
			return;
		}
		if (normalAP.getType() == PDFObject.DICTIONARY) {
			this.onAppearance = normalAP.getDictRef("On");
			this.offAppearance = normalAP.getDictRef("Off");
			PDFObject as = dictRef.getDictRef("AS");			
			this.appearanceStateOn = (as != null) && ("On".equals(as.getStringValue()));
		} else {
			this.onAppearance = normalAP;
			this.offAppearance = null;
			appearanceStateOn = true;
		}
		parseCommands();
	}

	private void parseCommands() throws IOException {
		if (onAppearance != null) {
			onCmd = parseCommand(onAppearance);
		}
		if (offAppearance != null) {
			offCmd = parseCommand(offAppearance);
		}
	}

	private List<PDFCmd> parseCommand(PDFObject obj) throws IOException {
        String type = obj.getDictRef("Subtype").getStringValue();
        if (type == null) {
            type = obj.getDictRef ("S").getStringValue ();
        }
        ArrayList<PDFCmd> result = new ArrayList<>();
        result.add(PDFPage.createPushCmd());
        result.add(PDFPage.createPushCmd());
        if (type.equals("Image")) {
            // stamp annotation transformation
            AffineTransform rectAt = getPositionTransformation();
            result.add(PDFPage.createXFormCmd(rectAt));
            
        	PDFImage img = PDFImage.createImage(obj, new HashMap<>() , false);
        	result.add(PDFPage.createImageCmd(img));
        } else if (type.equals("Form")) {
        	
            // rats.  parse it.
            PDFObject bobj = obj.getDictRef("BBox");
            float xMin = bobj.getAt(0).getFloatValue();
            float yMin = bobj.getAt(1).getFloatValue();
			float xMax = bobj.getAt(2).getFloatValue();
			float yMax = bobj.getAt(3).getFloatValue();
			Float bbox = new Float(xMin,
                    yMin,
                    xMax - xMin,
                    yMax - yMin);
            PDFPage formCmds = new PDFPage(bbox, 0);
            
            // stamp annotation transformation
            AffineTransform rectAt = getPositionTransformation();           
           formCmds.addXform(rectAt);
           
           AffineTransform rectScaled = getScalingTransformation(bbox);
           formCmds.addXform(rectScaled);
           
           
           

            // form transformation
            AffineTransform at;
            PDFObject matrix = obj.getDictRef("Matrix");
            if (matrix == null) {
                at = new AffineTransform();
            } else {
                float[] elts = new float[6];
                for (int i = 0; i < elts.length; i++) {
                    elts[i] = (matrix.getAt(i)).getFloatValue();
                }
                at = new AffineTransform(elts);
            }
            formCmds.addXform(at);
            
            HashMap<String,PDFObject> r = new HashMap<>(new HashMap<>());
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

	private void parsePopupAnnotation(PDFObject popupObj) throws IOException {
		this.popupAnnotation = (popupObj != null)?createAnnotation(popupObj):null;
	}

	/**
	 * <p>Getter for the field <code>iconName</code>.</p>
	 *
	 * @return the iconName
	 */
	public String getIconName() {
		return iconName;
	}

	/**
	 * <p>Getter for the field <code>popupAnnotation</code>.</p>
	 *
	 * @return the popupAnnotation
	 */
	public PDFAnnotation getPopupAnnotation() {
		return popupAnnotation;
	}

	/**
	 * <p>Getter for the field <code>onAppearance</code>.</p>
	 *
	 * @return the onAppearance
	 */
	public PDFObject getOnAppearance() {
		return onAppearance;
	}

	/**
	 * <p>Getter for the field <code>offAppearance</code>.</p>
	 *
	 * @return the offAppearance
	 */
	public PDFObject getOffAppearance() {
		return offAppearance;
	}

	/**
	 * <p>isAppearanceStateOn.</p>
	 *
	 * @return the appearanceStateOn
	 */
	public boolean isAppearanceStateOn() {
		return appearanceStateOn;
	}

	/**
	 * <p>switchAppearance.</p>
	 */
	public void switchAppearance() {
		this.appearanceStateOn = !this.appearanceStateOn;
	}

	/**
	 * <p>getCurrentAppearance.</p>
	 *
	 * @return a {@link PDFObject} object.
	 */
	public PDFObject getCurrentAppearance() {
		return appearanceStateOn?onAppearance:offAppearance;
	}

	/**
	 * <p>getCurrentCommand.</p>
	 *
	 * @return a {@link List} object.
	 */
	public List<PDFCmd> getCurrentCommand() {
		return appearanceStateOn?onCmd:offCmd;
	}

	/** {@inheritDoc} */
	@Override
	public List<PDFCmd> getPageCommandsForAnnotation() {
		List<PDFCmd> pageCommandsForAnnotation = super.getPageCommandsForAnnotation();
		pageCommandsForAnnotation.addAll(getCurrentCommand());
		return pageCommandsForAnnotation;
	}
}
