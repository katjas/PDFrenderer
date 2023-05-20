package org.loboevolution.pdfview.action;

import java.io.IOException;

import org.loboevolution.pdfview.PDFDestination;
import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFParseException;

/**
 * An action which specifies going to a particular destination
 *
  *
  *
 */
public class GoToAction extends PDFAction {
    /** the destination to go to */
    private final PDFDestination dest;
    
    /**
     * Creates a new instance of GoToAction from an object
     *
     * @param obj the PDFObject with the action information
     * @param root a {@link org.loboevolution.pdfview.PDFObject} object.
     * @throws java.io.IOException if any.
     */
    public GoToAction(PDFObject obj, PDFObject root) throws IOException {
        super("GoTo");
        
        // find the destination
        PDFObject destObj = obj.getDictRef("D");
        if (destObj == null) {
            throw new PDFParseException("No destination in GoTo action " + obj);
        }
        
        // parse it
        this.dest = PDFDestination.getDestination(destObj, root);
    }
    
    /**
     * Create a new GoToAction from a destination
     *
     * @param dest a {@link org.loboevolution.pdfview.PDFDestination} object.
     */
    public GoToAction(PDFDestination dest) {
        super("GoTo");
    
        this.dest = dest;
    }
      
    /**
     * Get the destination this action refers to
     *
     * @return a {@link org.loboevolution.pdfview.PDFDestination} object.
     */
    public PDFDestination getDestination() {
        return this.dest;
    }
}
