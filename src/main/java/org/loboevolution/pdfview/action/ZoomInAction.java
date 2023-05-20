package org.loboevolution.pdfview.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.loboevolution.pdfview.main.PDFViewer;

/**
 * <p>ZoomInAction class.</p>
 *
  *
  *
 */
public class ZoomInAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	private final PDFViewer dialog;
	
	private final float zoomfactor;

	/**
	 * <p>Constructor for ZoomInAction.</p>
	 *
	 * @param dialog a {@link org.loboevolution.pdfview.PDFViewer} object.
	 * @param factor a float.
	 */
	public ZoomInAction(PDFViewer dialog, float factor) {
		super("Zoom in", dialog.getIcon("/org/loboevolution/images/zoomin.png"));
		zoomfactor = factor;
		this.dialog = dialog;
	}

	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent evt) {
		dialog.doZoom(zoomfactor);
	}
}
