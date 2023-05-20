package org.loboevolution.pdfview.annotation;

import java.io.IOException;

import org.loboevolution.pdfview.PDFDestination;
import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFParseException;
import org.loboevolution.pdfview.action.GoToAction;
import org.loboevolution.pdfview.action.PDFAction;

/**
 ***************************************************************************
 * PDF annotation describing a link to either a location within the current
 * document, a location in another PDF file, an application/file to be opened
 * or a web site.
 * In the PDF structure a link can be a destination ("DEST") or an action ("A").
 * Both ways are handled as actions internally, i.e. for getting the links
 * destination, you should get the action from this annotation object. It can be
 * one of the following actions:
 * <p>GotoAction - for a file internal destination</p>
 * <p>GoToRAction - for a destination in a remote PDF file</p>
 * <p>GoToEAction - for a destination in an embedded PDF file</p>
 * <p>UriAction - for a web link</p>
 * <p>LaunchAction - for launching an application/opening a file</p>
 *
 * Author Katja Sondermann
 * @since 06.07.2009
 ***************************************************************************
  *
 */
public class LinkAnnotation extends PDFAnnotation {

	private PDFAction action = null;

	/**
	 ***********************************************************************
	 * Constructor
	 *
	 * @param annotObject a {@link PDFObject} object.
	 * @throws IOException if any.
	 */
	public LinkAnnotation(PDFObject annotObject) throws IOException {
		super(annotObject, ANNOTATION_TYPE.LINK);
		// a link annotation can either have an action (GoTo or URI) or a destination (DEST)
		PDFObject actionObj = annotObject.getDictRef("A");
		if (actionObj != null) {
			this.action = PDFAction.getAction(actionObj, annotObject.getRoot());
		} else {
			// if a destination is given, create a GoToAction from it
			PDFObject dest = annotObject.getDictRef("Dest");
			if (dest == null) {
				dest = annotObject.getDictRef("DEST");
			}
			if (dest != null) {
				this.action = new GoToAction(PDFDestination.getDestination(dest, annotObject.getRoot()));
			} else {
				throw new PDFParseException(
					"Could not parse link annotation (no Action or Destination found): "
						+ annotObject.toString());
			}
		}
	}
	
	/**
	 ***********************************************************************
	 * Get the contained PDFAction
	 *
	 * @return a {@link PDFAction} object.
	 */
	public PDFAction getAction() {
		return this.action;
	}
}
