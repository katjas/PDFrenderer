package org.loboevolution.pdfview.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.loboevolution.pdfview.main.PDFViewer;

/**
 * <p>PrintAction class.</p>
 *
  *
  *
 */
public class PrintAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	private final PDFViewer dialog;
	
	/**
	 * <p>Constructor for PrintAction.</p>
	 *
	 * @param dialog a {@link org.loboevolution.pdfview.PDFViewer} object.
	 */
	public PrintAction(PDFViewer dialog) {
		super("Print...", dialog.getIcon("/org/loboevolution/images/print.png"));
		this.dialog = dialog;
	}

	/** {@inheritDoc} */
	@Override
	public void actionPerformed(final ActionEvent e) {
		dialog.doPrint();

	}
}
