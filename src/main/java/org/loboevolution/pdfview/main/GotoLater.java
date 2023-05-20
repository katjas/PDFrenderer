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

import org.loboevolution.pdfview.main.ThumbPanel;

/**
 * Simple runnable to tell listeners that the page has changed.
 */
public class GotoLater implements Runnable {

	/** The page. */
	private final int page;
	
	private final ThumbPanel thumb;

	/**
	 * <p>Constructor for GotoLater.</p>
	 *
	 * @param pagenum a int.
	 * @param thumb a {@link ThumbPanel} object.
	 */
	public GotoLater(int pagenum,  ThumbPanel thumb) {
		page = pagenum;
		this.thumb = thumb;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		if (thumb.getListener() != null) {
			thumb.getListener().gotoPage(page);
		}
	}
}
