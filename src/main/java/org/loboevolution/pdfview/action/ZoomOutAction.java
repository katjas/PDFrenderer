package org.loboevolution.pdfview.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.loboevolution.pdfview.main.PDFViewer;

/**
 * <p>ZoomOutAction class.</p>
 *
  *
  *
 */
public class ZoomOutAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	private final PDFViewer dialog;
	
	private final float zoomfactor;

	/**
	 * <p>Constructor for ZoomOutAction.</p>
	 *
	 * @param dialog a {@link org.loboevolution.pdfview.PDFViewer} object.
	 * @param factor a float.
	 */
	public ZoomOutAction(PDFViewer dialog, float factor) {
		super("Zoom out", dialog.getIcon("/org/loboevolution/images/zoomout.png"));
		zoomfactor = factor;
		this.dialog = dialog;
	}

	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent evt) {
		dialog.doZoom(zoomfactor);
	}
}
