/*
 * $Id: PDFPrintPage.java,v 1.2 2007/12/20 18:33:33 rbair Exp $
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

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * A class representing a print job for a particular PDFFile.  The class
 * maintains a status dialog as it prints, allowing the user to cancel
 * the print job.
 */
public class PDFPrintPage implements Printable {
    /**
     * The PDFFile to be printed
     */
    private PDFFile file;

    /**
     * The PrinterJob for this print job
     */
    private PrinterJob pjob;

    /**
     * A dialog box indicating printing status, with cancel button
     */
    private JDialog pd;

    /**
     * The text in the progress dialog indicating the current page
     */
    private JLabel pagenumlabel;

    /**
     * The cancel button in the progress dialog
     */
    private JButton cancel;

    /**
     * Create a new PDFPrintPage object for a particular PDFFile.
     *
     * @param file the PDFFile to be printed.
     */
    public PDFPrintPage(PDFFile file) {
        this.file = file;
    }

    /**
     * Generates the status dialog with cancel button.
     */
    private void createPrintDialog() {
        pd = new JDialog((Frame) null, "Printing...", false);
        Container top = pd.getContentPane();
        Box lines = Box.createVerticalBox();
        Box line = Box.createHorizontalBox();
        line.add(new JLabel("Now printing: "));
        JLabel title = new JLabel("file.pdf");
        line.add(title);
        lines.add(line);

        line = Box.createHorizontalBox();
        line.add(Box.createHorizontalStrut(10));
        line.add(new JLabel("page "));
        pagenumlabel = new JLabel("1");
        line.add(pagenumlabel);
        line.add(new JLabel(" of "));
        JLabel totalpages = new JLabel(String.valueOf(file.getNumPages()));
        line.add(totalpages);
        lines.add(line);

        top.add(lines, BorderLayout.CENTER);

        Box cancelbox = Box.createHorizontalBox();
        cancelbox.add(Box.createHorizontalGlue());
        cancel = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent evt) {
                doCancel();
            }
        });
        cancelbox.add(cancel);
        top.add(cancelbox, BorderLayout.SOUTH);
    }

    /**
     * Show the progress dialog for this print job
     *
     * @param pjob the PrinterJob representing the print job
     */
    public void show(PrinterJob pjob) {
        this.pjob = pjob;
        if (pd == null) {
            createPrintDialog();
        }
        pd.pack();
        pd.show();
    }

    /**
     * Close the progress dialog.  Don't use this method to cancel
     * the print job; use {@link #doCancel doCancel} instead.
     */
    public void hide() {
        pd.dispose();
    }

    /**
     * Cancel the print job.  Disables the cancel button, as it might
     * take a while for the cancel to take effect.
     */
    public void doCancel() {
        cancel.setEnabled(false);
        pjob.cancel();
    }

    // from Printable interface:  prints a single page, given a Graphics
    // to draw into, the page format, and the page number.
    public int print(Graphics g, PageFormat format, int index)
            throws PrinterException {
        int pagenum = index + 1;

        // don't bother if the page number is out of range.
        if ((pagenum >= 1) && (pagenum <= file.getNumPages())) {

            // update the page number in the progress dialog
            if (pagenumlabel != null) {
                pagenumlabel.setText(String.valueOf(pagenum));
            }

            // fit the PDFPage into the printing area
            Graphics2D g2 = (Graphics2D) g;
            PDFPage page = file.getPage(pagenum);
            double pwidth = format.getImageableWidth();
            double pheight = format.getImageableHeight();

            double aspect = page.getAspectRatio();
            double paperaspect = pwidth / pheight;

            Rectangle imgbounds;

            if (aspect > paperaspect) {
                // paper is too tall / pdfpage is too wide
                int height = (int) (pwidth / aspect);
                imgbounds = new Rectangle((int) format.getImageableX(),
                        (int) (format.getImageableY() +
                                ((pheight - height) / 2)),
                        (int) pwidth, height);
            } else {
                // paper is too wide / pdfpage is too tall
                int width = (int) (pheight * aspect);
                imgbounds = new Rectangle((int) (format.getImageableX() +
                        ((pwidth - width) / 2)),
                        (int) format.getImageableY(),
                        width, (int) pheight);
            }

            // debugging -- frame the page with a black border, outside the
            // imageable region.
            //
            //	    double ix= format.getImageableX();
            //	    double iy= format.getImageableY();
            //	    double iw= format.getImageableWidth();
            //	    double ih= format.getImageableHeight();
            //	    double tw= format.getWidth();
            //	    double th= format.getHeight();
            //	    g2.fill(new Rectangle2D.Double(0, 0, tw, iy));
            //	    g2.fill(new Rectangle2D.Double(0, 0, ix, th));
            //	    g2.fill(new Rectangle2D.Double(0, iy+ih, tw, th-iy-ih));
            //	    g2.fill(new Rectangle2D.Double(ix+iw, 0, tw-ix-iw, th));

            // render the page
            PDFRenderer pgs =
                    new PDFRenderer(page, g2, imgbounds, null, null);
            try {
                page.waitForFinish();
                pgs.run();
            } catch (InterruptedException ie) {
            }
            return PAGE_EXISTS;
        } else {
            return NO_SUCH_PAGE;
        }
    }

}
