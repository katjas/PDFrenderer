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

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.loboevolution.pdfview.PDFFile;

/**
 * <p>NavigationToolbar class.</p>
 */
public class NavigationToolbar extends JToolBar {
	
	private static final long serialVersionUID = 1L;

	private static final int FIRSTPAGE = 0;
	
	private static final int FBACKPAGE = 1;
	
	private static final int BACKPAGE = 2;
	
	private static final int FORWARDPAGE = 3;
	
	private static final int FFORWARDPAGE = 4;
	
	private static final int LASTPAGE = 5;
	
	private static final int SETPAGE = 6;

	/** the current page number text field. */
	protected final JTextField currentPageBox = new JTextField(4);
	
	private final JLabel totalNoOfPages = new JLabel();

	private final PDFViewer PDFViewer;

	/**
	 * <p>Constructor for NavigationToolbar.</p>
	 *
	 * @param PDFViewer a {@link org.loboevolution.pdf.PDFViewer} object.
	 */
	public NavigationToolbar(PDFViewer PDFViewer) {
		this.PDFViewer = PDFViewer;

		add(Box.createHorizontalGlue());

		addButton("Rewind To Start", "/org/loboevolution/images/start.gif", FIRSTPAGE);
		addButton("Back 5 Pages", "/org/loboevolution/images/fback.gif", FBACKPAGE);
		addButton("Back", "/org/loboevolution/images/back.gif", BACKPAGE);

		add(new JLabel("Page"));
		currentPageBox.setText("1");
		currentPageBox.setMaximumSize(new Dimension(5, 50));
		currentPageBox.addActionListener(actionEvent -> executeCommand(SETPAGE));
		add(currentPageBox);
		add(totalNoOfPages);

		addButton("Forward", "/org/loboevolution/images/forward.gif", FORWARDPAGE);
		addButton("Forward 5 Pages", "/org/loboevolution/images/fforward.gif", FFORWARDPAGE);
		addButton("Fast Forward To End", "/org/loboevolution/images/end.gif", LASTPAGE);

		add(Box.createHorizontalGlue());

	}

	/**
	 * <p>Setter for the field <code>totalNoOfPages</code>.</p>
	 *
	 * @param noOfPages a int.
	 */
	public void setTotalNoOfPages(int noOfPages) {
		totalNoOfPages.setText("of " + noOfPages);
	}

	/**
	 * <p>setCurrentPage.</p>
	 *
	 * @param currentPage a int.
	 */
	public void setCurrentPage(int currentPage) {
		currentPageBox.setText(String.valueOf(currentPage));
	}

    private void addButton(String tooltip, String url, final int type) {
		JButton button = new JButton();
		button.setIcon(new ImageIcon(getClass().getResource(url)));
		button.setToolTipText(tooltip);
		button.addActionListener(actionEvent -> executeCommand(type));

		add(button);
	}

	/**
	 * <p>executeCommand.</p>
	 *
	 * @param type a int.
	 */
	public void executeCommand(int type) {
		switch (type) {
		case FIRSTPAGE:
			PDFViewer.doFirst();
			break;
		case FBACKPAGE:
			PDFViewer.gotoPage(PDFViewer.curpage - 5);
			break;
		case BACKPAGE:
			PDFViewer.doPrev();
			break;
		case FORWARDPAGE:
			PDFViewer.doNext();
			break;
		case FFORWARDPAGE:
			PDFViewer.gotoPage(PDFViewer.curpage + 5);
			break;
		case LASTPAGE:
			PDFViewer.doLast();
			break;
		case SETPAGE:
			int pagenum = -1;
			final PDFFile curFile = PDFViewer.curFile;
			final int curpage = PDFViewer.curpage;
			try {
				pagenum = Integer.parseInt(currentPageBox.getText()) - 1;
			} catch (NumberFormatException nfe) {
			}
			if (pagenum >= curFile.getNumPages()) {
				pagenum = curFile.getNumPages() - 1;
			}
			if (pagenum >= 0) {
				if (pagenum != curpage) {
					PDFViewer.gotoPage(pagenum);
				}
			} else {
				currentPageBox.setText(String.valueOf(curpage));
			}
			break;
		default:
			break;
		}
		currentPageBox.setText(String.valueOf(PDFViewer.curpage + 1));
	}
}
