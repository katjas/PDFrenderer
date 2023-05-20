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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.loboevolution.pdfview.PDFFile;
import org.loboevolution.pdfview.PDFPage;

/**
 * A panel of thumbnails, one for each page of a PDFFile. You can add a
 * PageChangeListener to be informed of when the user clicks one of the pages.
 */
public class ThumbPanel extends JPanel implements Runnable, Scrollable, ImageObserver {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6761217072379594185L;
	
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(ThumbPanel.class.getName());

	/** The PDFFile being displayed. */
	private transient PDFFile file;

	/** Array of images, one per page in the file. */
	private transient Image[] images;

	/** Size of the border between images. */
	private final int border = 2;
	
	/**
	 * Height of each line. Thumbnails will be scaled to this height (minus the
	 * border).
	 */
	private final int lineheight = 96 + border;
	
	/**
	 * Guesstimate of the width of a thumbnail that hasn't been processed yet.
	 */
	private int defaultWidth = (lineheight - border) * 4 / 3;
	
	/**
	 * Array of the x locations of each of the thumbnails. Every 0 stored in
	 * this array indicates the start of a new line of thumbnails.
	 */
	private int[] xloc;

	/** Thread that renders each thumbnail in turn. */
	private transient Thread anim;
	
	/** Which thumbnail is selected, or -1 if no thumbnail selected. */
	private int showing = -1;
	
	/**
	 * Which thumbnail needs to be drawn next, or -1 if the previous needy
	 * thumbnail is being processed.
	 */
	private int needdrawn = -1;
	
	/**
	 * Whether the default width has been guesstimated for this PDFFile yet.
	 */
	private boolean defaultNotSet = true;

	/** The PageChangeListener that is listening for page changes. */
	private transient PageChangeListener listener;

	/**
	 * Creates a new ThumbPanel based on a PDFFile. The file may be null.
	 * Automatically starts rendering thumbnails for that file.
	 *
	 * @param file a {@link PDFFile} object.
	 */
	public ThumbPanel(PDFFile file) {
		createAndShowGUI(file);
	}

	private void createAndShowGUI(PDFFile file) {
		this.file = file;
		if (file != null) {
			int count = file.getNumPages();
			images = new Image[count];
			xloc = new int[count];
			setPreferredSize(new Dimension(defaultWidth, 200));
			addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(final MouseEvent evt) {
					handleClick(evt.getX(), evt.getY());
				}
			});
			anim = new Thread(this);
			anim.setName(getClass().getName());
			anim.start();
		} else {
			images = new Image[0];
			setPreferredSize(new Dimension(defaultWidth, 200));
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Renders each of the pages in the PDFFile into a thumbnail. Preferentially
	 * works on the needdrawn thumbnail, otherwise, go in order.
	 */
	@Override
	public void run() {
		int workingon = 0; // the thumbnail we'll be rendering next.

		while (anim == Thread.currentThread()) {

			if (needdrawn >= 0) {
				workingon = needdrawn;
				needdrawn = -1;
			}

			// find an unfinished page
			int loop;
			for (loop = images.length; loop > 0; loop--) {
				if (images[workingon] == null) {
					break;
				}
				workingon++;
				if (workingon >= images.length) {
					workingon = 0;
				}
			}
			if (loop == 0) {
				// done all pages.
				break;
			}

			// build the page
			try {
				int pagetoread = workingon + 1;
				PDFPage p = file.getPage(pagetoread, true);
				int wid = (int) Math.ceil((lineheight - border) * p.getAspectRatio());
				int pagetowrite = workingon;

				Image i = p.getImage(wid, lineheight - border, null, this, true, true);
				images[pagetowrite] = i;

				if (defaultNotSet) {
					defaultNotSet = false;
					setDefaultWidth(wid);
				}
				repaint();
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);

				int size = lineheight - border;
				images[workingon] = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_BINARY);
			}
		}
	}

	/**
	 * Adds a PageChangeListener to receive notification of page clicks.
	 *
	 * @param pl a {@link org.loboevolution.pdf.PageChangeListener} object.
	 */
	public void addPageChangeListener(PageChangeListener pl) {
		// [[MW: should be an array list instead of only one]]
		setListener(pl);
	}

	/**
	 * Removes a PageChangeListener from the notification list.
	 */
	public void removePageChangeListener() {
		// [[MW: should be an array list instead of only one]]
		setListener(null);
	}

	/**
	 * Stops the render thread. Be sure to call this before dropping a
	 * ThumbPanel.
	 */
	public void stop() {
		anim = null;
	}

	/**
	 * Sets the guesstimate of the width of a thumbnail that hasn't been
	 * processed yet.
	 *
	 * @param width
	 *            the new guesstimate of the width of a thumbnail that hasn't
	 *            been processed yet
	 */
	public void setDefaultWidth(int width) {
		defaultWidth = width;
		// setPreferredSize(new Dimension(width, lineheight));
	}

	/**
	 * Handles a mouse click in the panel. Figures out which page was clicked,
	 * and calls showPage.
	 *
	 * @param x
	 *            the x coordinate of the mouse click
	 * @param y
	 *            the y coordinate of the mouse click
	 */
	public void handleClick(int x, int y) {
		int linecount = -1;
		int line = y / lineheight;
		// run through the thumbnail locations, counting new lines
		// until the appropriate line is reached.
		for (int i = 0; i < xloc.length; i++) {
			if (xloc[i] == 0) {
				linecount++;
			}
			if (line == linecount && xloc[i] + (images[i] != null ? images[i].getWidth(null) : defaultWidth) > x) {
				showPage(i);
				break;
			}
		}
	}

	/**
	 * Sets the currently viewed page, indicates it with a highlight border, and
	 * makes sure the thumbnail is visible.
	 *
	 * @param pagenum a int.
	 */
	public void pageShown(int pagenum) {
		if (showing != pagenum) {
			// FIND THE SELECTION RECTANGLE
			// getViewPort.scrollRectToVisible(r);
			if (pagenum >= 0 && getParent() instanceof JViewport) {
				int y = -lineheight;
				for (int i = 0; i <= pagenum; i++) {
					if (xloc[i] == 0) {
						y += lineheight;
					}
				}
				Rectangle r = new Rectangle(xloc[pagenum], y,
						images[pagenum] == null ? defaultWidth : images[pagenum].getWidth(null), lineheight);
				scrollRectToVisible(r);
			}
			showing = pagenum;
			repaint();
		}
	}

	/**
	 * Notifies the listeners that a page has been selected. Performs the
	 * notification in the AWT thread. Also highlights the selected page. Does
	 * this first so that feedback is immediate.
	 *
	 * @param pagenum a int.
	 */
	public void showPage(int pagenum) {
		pageShown(pagenum);
		SwingUtilities.invokeLater(new GotoLater(pagenum, this));
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Updates the positions of the thumbnails, and draws them to the screen.
	 */
	@Override
	public void paint(final Graphics g) {
		int x = 0;
		int y = 0;
		int maxwidth = 0;
		Rectangle clip = g.getClipBounds();
		g.setColor(Color.gray);
		int width = getWidth();
		g.fillRect(0, 0, width, getHeight());

		for (int i = 0; i < images.length; i++) {
			// calculate the x location of the thumbnail, based on its width
			int w = defaultWidth + 2;
			if (images[i] != null) {
				w = images[i].getWidth(null) + 2;
			}
			// need a new line?
			if (x + w > width && x != 0) {
				x = 0;
				y += lineheight;
			}
			// if the thumbnail is visible, draw it.
			if (clip.intersects(new Rectangle(x, y, w, lineheight))) {
				if (images[i] != null) {
					// thumbnail is ready.
					g.drawImage(images[i], x + 1, y + 1, this);
				} else {
					// thumbnail isn't ready. Remember that we need it...
					if (needdrawn == -1) {
						needdrawn = i;
					}
					// ... and draw a blank thumbnail.
					g.setColor(Color.lightGray);
					g.fillRect(x + 1, y + 1, w - border, lineheight - border);
					g.setColor(Color.darkGray);
					g.drawRect(x + 1, y + 1, w - border - 1, lineheight - border - 1);
				}
				// draw the selection highlight if needed.
				if (i == showing) {
					g.setColor(Color.red);
					g.drawRect(x, y, w - 1, lineheight - 1);
					g.drawRect(x + 1, y + 1, w - 3, lineheight - 3);
				}
			}
			// save the x location of this thumbnail.
			xloc[i] = x;
			x += w;
			// remember the longest line
			if (x > maxwidth) {
				maxwidth = x;
			}
		}
		// if there weren't any thumbnails, make a default line width
		if (maxwidth == 0) {
			maxwidth = defaultWidth;
		}
		Dimension d = getPreferredSize();
		if (d.height != y + lineheight || d.width != maxwidth) {
			setPreferredSize(new Dimension(maxwidth, y + lineheight));
			revalidate();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Handles notification of any image updates. Not used any more.
	 */
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		// if ((infoflags & ALLBITS)!=0) {
		// flag.set();
		// }
		return (infoflags & (ALLBITS | ERROR | ABORT)) == 0;
	}

	/** {@inheritDoc} */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/** {@inheritDoc} */
	@Override
	public int getScrollableBlockIncrement(Rectangle visrect, int orientation, int direction) {
		return Math.max(lineheight, visrect.height / lineheight * lineheight);
	}

	/** {@inheritDoc} */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public int getScrollableUnitIncrement(Rectangle visrect, int orientation, int direction) {
		return lineheight;
	}

	/**
	 * <p>Getter for the field <code>listener</code>.</p>
	 *
	 * @return the listener
	 */
	public PageChangeListener getListener() {
		return listener;
	}

	/**
	 * <p>Setter for the field <code>listener</code>.</p>
	 *
	 * @param listener the listener to set
	 */
	public void setListener(PageChangeListener listener) {
		this.listener = listener;
	}
}
