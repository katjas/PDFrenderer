package org.loboevolution.pdfview.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.loboevolution.pdfview.main.PDFViewer;

/**
 * <p>FitAction class.</p>
 *
  *
  *
 */
public class FitAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	private final PDFViewer dialog;
	
	/**
	 * <p>Constructor for FitAction.</p>
	 *
	 * @param dialog a {@link org.loboevolution.pdfview.PDFViewer} object.
	 */
	public FitAction(PDFViewer dialog) {
		super("Fit", dialog.getIcon("/org/loboevolution/images/fit.png"));
		this.dialog = dialog;
	}

	/** {@inheritDoc} */
	@Override
	public void actionPerformed(final ActionEvent e) {
		dialog.doFit(true, true);

	}
}
