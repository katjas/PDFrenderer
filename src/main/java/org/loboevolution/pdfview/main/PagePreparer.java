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

import java.awt.Dimension;

import org.loboevolution.pdfview.main.PDFViewer;
import org.loboevolution.pdfview.PDFFile;
import org.loboevolution.pdfview.PDFPage;

/**
 * A class to pre-cache the next page for better UI response.
 */
class PagePreparer extends Thread {

	/** The waitfor page. */
	private int waitforPage;

	/** The prep page. */
	private final int prepPage;
	
	private final PDFViewer PDFViewer;

	/**
	 * Creates a new PagePreparer to prepare the page after the current one.
	 *
	 * @param waitforPage
	 *            the current page number, 0 based
	 * @param PDFViewer a {@link org.loboevolution.pdfview.main.PDFViewer} object.
	 */
	public PagePreparer(int waitforPage, PDFViewer PDFViewer) {
		setDaemon(true);
		setName(getClass().getName());
		this.waitforPage = waitforPage;
		this.prepPage = waitforPage + 1;
		this.PDFViewer = PDFViewer;
	}

	/**
	 * Quit.
	 */
	public void quit() {
		waitforPage = -1;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		Dimension size = null;
		final PagePanel fspp = PDFViewer.fspp;
		final PagePanel page = PDFViewer.page;
		final int curpage = PDFViewer.curpage;
		final PDFFile curFile = PDFViewer.curFile;
		
		if (fspp != null) {
			fspp.waitForCurrentPage();
			size = fspp.getCurSize();
		} else if (page != null) {
			page.waitForCurrentPage();
			size = page.getCurSize();
		}
		if (waitforPage == curpage) {
			PDFPage pdfPage = curFile.getPage(prepPage + 1, true);
			if (pdfPage != null && waitforPage == curpage) {
				pdfPage.getImage(size.width, size.height, null, null, true, true);
			}
		}
	}
}
