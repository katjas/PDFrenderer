/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.loboevolution.pdfview.main;

import org.loboevolution.pdfview.main.PDFPrintPage;
import org.loboevolution.pdfview.main.PDFViewer;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JOptionPane;

/**
 * A thread for printing in.
 *
  *
  *
 */
public class PrintThread extends Thread {

	/** The pt pages. */
	private final PDFPrintPage ptPages;

	/** The pt pjob. */
	private final PrinterJob ptPjob;
	
	private final PDFViewer dialog;

	/**
	 * Instantiates a new prints the thread.
	 *
	 * @param pages
	 *            the pages
	 * @param pjob
	 *            the pjob
	 * @param dialog a {@link PDFViewer} object.
	 */
	public PrintThread(PDFPrintPage pages, PrinterJob pjob, PDFViewer dialog) {
		ptPages = pages;
		ptPjob = pjob;
		setName(getClass().getName());
		this.dialog = dialog;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try {
			ptPages.show(ptPjob);
			ptPjob.print();
		} catch (PrinterException pe) {
			JOptionPane.showMessageDialog(dialog, "Printing Error: " + pe.getMessage(), "Print Aborted",
					JOptionPane.ERROR_MESSAGE);
		}
		ptPages.hide();
	}
}
