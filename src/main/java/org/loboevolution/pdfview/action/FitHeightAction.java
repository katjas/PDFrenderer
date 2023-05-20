package org.loboevolution.pdfview.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.loboevolution.pdfview.main.PDFViewer;

/**
 * <p>FitHeightAction class.</p>
 *
  *
  *
 */
public class FitHeightAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	private final PDFViewer dialog;
	
	/**
	 * <p>Constructor for FitHeightAction.</p>
	 *
	 * @param dialog a {@link org.loboevolution.pdfview.PDFViewer} object.
	 */
	public FitHeightAction(PDFViewer dialog) {
		super("Fit Height", dialog.getIcon("/org/loboevolution/images/fit-height.png"));
		this.dialog = dialog;
	}

	/** {@inheritDoc} */
	@Override
	public void actionPerformed(final ActionEvent e) {
		dialog.doFit(false, false);

	}
}
