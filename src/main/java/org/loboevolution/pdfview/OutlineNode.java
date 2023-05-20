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
package org.loboevolution.pdfview;

import javax.swing.tree.DefaultMutableTreeNode;

import org.loboevolution.pdfview.action.PDFAction;

/**
 * <p>OutlineNode class.</p>
 *
  *
  *
 */
public class OutlineNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

    private final String title;

    /**
     * Create a new outline node
     *
     * @param title the node's visible name in the tree
     */
    public OutlineNode(String title) {
        this.title = title;
    }

    /**
     * Get the PDF action associated with this node
     *
     * @return a {@link org.loboevolution.pdfview.action.PDFAction} object.
     */
    public PDFAction getAction() {
        return (PDFAction) getUserObject();
    }

    /**
     * Set the PDF action associated with this node
     *
     * @param action a {@link org.loboevolution.pdfview.action.PDFAction} object.
     */
    public void setAction(PDFAction action) {
        setUserObject(action);
    }

    /**
     * {@inheritDoc}
     *
     * Return the node's visible name in the tree
     */
    @Override
    public String toString() {
        return this.title;
    }
}
