/*
 * $Id: PagePanel.java,v 1.2 2007/12/20 18:33:33 rbair Exp $
 *
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

package com.sun.pdfview.main;

import com.sun.pdfview.PDFPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

/**
 * A Swing-based panel that displays a PDF page image.  If the zoom tool
 * is in use, allows the user to select a particular region of the image to
 * be zoomed.
 */
public class PagePanel extends JPanel
        implements ImageObserver, MouseListener, MouseMotionListener {
    /**
     * The image of the rendered PDF page being displayed
     */
    Image currentImage;

    /**
     * The current PDFPage that was rendered into currentImage
     */
    PDFPage currentPage;

    /* the current transform from device space to page space */
    AffineTransform currentXform;

    /**
     * The horizontal offset of the image from the left edge of the panel
     */
    int offx;

    /**
     * The vertical offset of the image from the top of the panel
     */
    int offy;

    /**
     * the current clip, in device space
     */
    Rectangle2D clip;

    /**
     * the clipping region used for the image
     */
    Rectangle2D prevClip;

    /**
     * the size of the image
     */
    Dimension prevSize;

    /**
     * the zooming marquee
     */
    Rectangle zoomRect;

    /**
     * whether the zoom tool is enabled
     */
    boolean useZoom = false;

    //    /** a listener for page changes */
    //    PageChangeListener listener;

    /**
     * a flag indicating whether the current page is done or not.
     */
    Flag flag = new Flag();

    // Color boxcolor= new Color(255,200,200);

    /**
     * Create a new PagePanel, with a default size of 800 by 600 pixels.
     */
    public PagePanel() {
        super();
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Stop the generation of any previous page, and draw the new one.
     *
     * @param page the PDFPage to draw.
     */
    public synchronized void showPage(PDFPage page) {
        // stop drawing the previous page
        if (currentPage != null && prevSize != null) {
            currentPage.stop(prevSize.width, prevSize.height, prevClip);
        }

        // set up the new page
        currentPage = page;

        if (page == null) {
            // no page
            currentImage = null;
            clip = null;
            currentXform = null;
            repaint();
        } else {
            // start drawing -- clear the flag to indicate we're in progress.
            flag.clear();
            //	    System.out.println("   flag cleared");

            Dimension sz = getSize();
            if (sz.width + sz.height == 0) {
                // no image to draw.
                return;
            }
            //	    System.out.println("Ratios: scrn="+((float)sz.width/sz.height)+
            //			       ", clip="+(clip==null ? 0 : clip.getWidth()/clip.getHeight()));

            // calculate the clipping rectangle in page space from the
            // desired clip in screen space.
            Rectangle2D useClip = clip;
            if (clip != null && currentXform != null) {
                useClip = currentXform.createTransformedShape(clip).getBounds2D();
            }

            Dimension pageSize = page.getUnstretchedSize(sz.width, sz.height,
                    useClip);

            // get the new image
            currentImage = page.getImage(pageSize.width, pageSize.height,
                    useClip, this);

            // calculate the transform from screen to page space
            currentXform = page.getInitialTransform(pageSize.width,
                    pageSize.height,
                    useClip);
            try {
                currentXform = currentXform.createInverse();
            } catch (NoninvertibleTransformException nte) {
                System.out.println("Error inverting page transform!");
                nte.printStackTrace();
            }

            prevClip = useClip;
            prevSize = pageSize;

            repaint();
        }
    }

    /**
     * @deprecated
     */
    public synchronized void flush() {
        //	images.clear();
        //	lruPages.clear();
        //	nextPage= null;
        //	nextImage= null;
    }

    /**
     * Draw the image.
     */
    public void paint(Graphics g) {
        Dimension sz = getSize();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (currentImage == null) {
            // No image -- draw an empty box
            // [[MW: remove the scary red X]]
            //	    g.setColor(Color.red);
            //	    g.drawLine(0, 0, getWidth(), getHeight());
            //	    g.drawLine(0, getHeight(), getWidth(), 0);
            g.setColor(Color.black);
            g.drawString("No page selected", getWidth() / 2 - 30, getHeight() / 2);
        } else {
            // draw the image
            int imwid = currentImage.getWidth(null);
            int imhgt = currentImage.getHeight(null);

            // draw it centered within the panel
            offx = (sz.width - imwid) / 2;
            offy = (sz.height - imhgt) / 2;

            if ((imwid == sz.width && imhgt <= sz.height) ||
                    (imhgt == sz.height && imwid <= sz.width)) {

                g.drawImage(currentImage, offx, offy, this);

            } else {
                // the image is bogus.  try again, or give up.
                flush();
                if (currentPage != null) {
                    showPage(currentPage);
                }
                g.setColor(Color.red);
                g.drawLine(0, 0, getWidth(), getHeight());
                g.drawLine(0, getHeight(), getWidth(), 0);
            }
        }
        // draw the zoomrect if there is one.
        if (zoomRect != null) {
            g.setColor(Color.red);
            g.drawRect(zoomRect.x, zoomRect.y,
                    zoomRect.width, zoomRect.height);
        }
        // debugging: draw a rectangle around the portion that just changed.
        //	g.setColor(boxColor);
        //	Rectangle r= g.getClipBounds();
        //	g.drawRect(r.x, r.y, r.width-1, r.height-1);
    }

    /**
     * Gets the page currently being displayed
     */
    public PDFPage getPage() {
        return currentPage;
    }

    /**
     * Gets the size of the image currently being displayed
     */
    public Dimension getCurSize() {
        return prevSize;
    }

    /**
     * Gets the clipping rectangle in page space currently being displayed
     */
    public Rectangle2D getCurClip() {
        return prevClip;
    }

    /**
     * Waits until the page is either complete or had an error.
     */
    public void waitForCurrentPage() {
        flag.waitForFlag();
    }

    /**
     * Handles notification of the fact that some part of the image
     * changed.  Repaints that portion.
     *
     * @return true if more updates are desired.
     */
    public boolean imageUpdate(Image img, int infoflags, int x, int y,
                               int width, int height) {
        // System.out.println("Image update: " + (infoflags & ALLBITS));
        Dimension sz = getSize();
        if ((infoflags & (SOMEBITS | ALLBITS)) != 0) {
            // [[MW: dink this rectangle by 1 to handle antialias issues]]
            repaint(x + offx, y + offy, width, height);
        }
        if ((infoflags & (ALLBITS | ERROR | ABORT)) != 0) {
            flag.set();
            //	    System.out.println("   flag set");
            return false;
        } else {
            return true;
        }
    }

//    public void addPageChangeListener(PageChangeListener pl) {
//	listener= pl;
//    }

//    public void removePageChangeListener(PageChangeListener pl) {
//	listener= null;
//    }

    /**
     * Turns the zoom tool on or off.  If on, mouse drags will draw the
     * zooming marquee.  If off, mouse drags are ignored.
     */
    public void useZoomTool(boolean use) {
        useZoom = use;
    }

    /**
     * Set the desired clipping region (in screen coordinates), and redraw
     * the image.
     */
    public void setClip(Rectangle2D clip) {
        this.clip = clip;
        showPage(currentPage);
    }

    /**
     * x location of the mouse-down event
     */
    int downx;

    /**
     * y location of the mouse-down event
     */
    int downy;

    /**
     * Handles a mousePressed event
     */
    public void mousePressed(MouseEvent evt) {
        downx = evt.getX();
        downy = evt.getY();
    }

    /**
     * Handles a mouseReleased event.  If zooming is turned on and there's
     * a valid zoom rectangle, set the image clip to the zoom rect.
     */
    public void mouseReleased(MouseEvent evt) {
        // calculate new clip
        if (!useZoom || zoomRect == null ||
                zoomRect.width == 0 || zoomRect.height == 0) {
            zoomRect = null;
            return;
        }

        setClip(new Rectangle2D.Double(zoomRect.x - offx, zoomRect.y - offy,
                zoomRect.width, zoomRect.height));

        zoomRect = null;
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mouseMoved(MouseEvent evt) {
    }

    /**
     * Handles a mouseDragged event. Constrains the zoom rect to the
     * aspect ratio of the panel unless the shift key is down.
     */
    public void mouseDragged(MouseEvent evt) {
        if (useZoom) {
            int x = evt.getX();
            int y = evt.getY();
            int dx = Math.abs(x - downx);
            int dy = Math.abs(y - downy);
            // constrain to the aspect ratio of the panel
            if ((evt.getModifiers() & evt.SHIFT_MASK) == 0) {
                float aspect = (float) dx / (float) dy;
                float waspect = (float) getWidth() / (float) getHeight();
                if (aspect > waspect) {
                    dy = (int) (dx / waspect);
                } else {
                    dx = (int) (dy * waspect);
                }
            }
            if (x < downx) {
                x = downx - dx;
            }
            if (y < downy) {
                y = downy - dy;
            }
            Rectangle old = zoomRect;
            // ignore small rectangles
            if (dx < 5 || dy < 5) {
                zoomRect = null;
            } else {
                zoomRect = new Rectangle(Math.min(downx, x), Math.min(downy, y),
                        dx, dy);
            }
            // calculate the repaint region.  Should be the union of the
            // old zoom rect and the new one, with an extra pixel on the
            // bottom and right because of the way rectangles are drawn.
            if (zoomRect != null) {
                if (old != null) {
                    old.add(zoomRect);
                } else {
                    old = new Rectangle(zoomRect);
                }
            }
            if (old != null) {
                old.width++;
                old.height++;
            }
            if (old != null) {
                repaint(old);
            }
        }
    }

}
