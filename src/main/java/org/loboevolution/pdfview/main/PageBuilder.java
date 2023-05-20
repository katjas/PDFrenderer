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

import java.io.Serializable;

/**
 * Combines numeric key presses to build a multi-digit page number.
 */
class PageBuilder implements Serializable, Runnable {

	private static final long serialVersionUID = 1L;

	/** The value. */
	private int value = 0;

	/** The timeout. */
	private long timeout;

	/** The anim. */
	private transient Thread anim;
	
	private final PDFViewer PDFViewer;
	
	/**
	 * <p>Constructor for PageBuilder.</p>
	 *
	 * @param PDFViewer a {@link org.loboevolution.pdfview.PDFViewer} object.
	 */
	public PageBuilder(PDFViewer PDFViewer) {
		this.PDFViewer = PDFViewer;
	}

	/**
	 * add the digit to the page number and start the timeout thread.
	 *
	 * @param keyval
	 *            the keyval
	 */
	public synchronized void keyTyped(int keyval) {
		value = value * 10 + keyval;
		timeout = System.currentTimeMillis() + 500;
		if (anim == null) {
			anim = new Thread(this);
			anim.setName(getClass().getName());
			anim.start();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * waits for the timeout, and if time expires, go to the specified page
	 * number.
	 */
	@Override
	public void run() {
		long now;
		long then;
		synchronized (this) {
			now = System.currentTimeMillis();
			then = timeout;
		}
		while (now < then) {
			try {
				Thread.sleep(timeout - now);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			synchronized (this) {
				now = System.currentTimeMillis();
				then = timeout;
			}
		}
		synchronized (this) {
			PDFViewer.gotoPage(value - 1);
			anim = null;
			value = 0;
		}
	}
}
