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

import java.io.IOException;

/**
 * Represents a destination in a PDF file. Destinations take 3 forms:
 * <ul>
 * <li>An explicit destination, which contains a reference to a page as well as
 * some stuff about how to fit it into the window.
 * <li>A named destination, which uses the PDF file's Dests entry in the
 * document catalog to map a name to an explicit destination
 * <li>A string destintation, which uses the PDF file's Dests entry. in the name
 * directory to map a string to an explicit destination.
 * </ul>
 *
 * All three of these cases are handled by the getDestination() method.
 *
  *
  *
 */
public class PDFDestination {

	/** The known types of destination */
	public static final int XYZ = 0;
	/** Constant <code>FIT=1</code> */
	public static final int FIT = 1;
	/** Constant <code>FITH=2</code> */
	public static final int FITH = 2;
	/** Constant <code>FITV=3</code> */
	public static final int FITV = 3;
	/** Constant <code>FITR=4</code> */
	public static final int FITR = 4;
	/** Constant <code>FITB=5</code> */
	public static final int FITB = 5;
	/** Constant <code>FITBH=6</code> */
	public static final int FITBH = 6;
	/** Constant <code>FITBV=7</code> */
	public static final int FITBV = 7;
	/** the type of this destination (from the list above) */
	private final int type;
	/** the page we refer to */
	private final PDFObject pageObj;
	/** the left coordinate of the fit area, if applicable */
	private float left;
	/** the right coordinate of the fit area, if applicable */
	private float right;
	/** the top coordinate of the fit area, if applicable */
	private float top;
	/** the bottom coordinate of the fit area, if applicable */
	private float bottom;
	/** the zoom, if applicable */
	private float zoom;

	/**
	 * Creates a new instance of PDFDestination
	 *
	 * @param pageObj
	 *            the page object this destination refers to
	 * @param type
	 *            the type of page this object refers to
	 */
	protected PDFDestination(PDFObject pageObj, int type) {
		this.pageObj = pageObj;
		this.type = type;
	}

	/**
	 * Get a destination from either an array (explicit destination), a name
	 * (named destination) or a string (name tree destination).
	 *
	 * @param obj
	 *            the PDFObject representing this destination
	 * @param root
	 *            the root of the PDF object tree
	 * @return a {@link org.loboevolution.pdfview.PDFDestination} object.
	 * @throws java.io.IOException if any.
	 */
	public static PDFDestination getDestination(PDFObject obj, PDFObject root) throws IOException {
		// resolve string and name issues
		if (obj.getType() == PDFObject.NAME) {
			obj = getDestFromName(obj, root);
		} else if (obj.getType() == PDFObject.STRING) {
			obj = getDestFromString(obj, root);
		}

		// make sure we have the right kind of object
		if (obj == null || obj.getType() != PDFObject.ARRAY) {
			throw new PDFParseException("Can't create destination from: " + obj);
		}

		// the array is in the form [page type args ... ]
		PDFObject[] destArray = obj.getArray();

		// create the destination based on the type
		PDFDestination dest = null;
		String type = destArray[1].getStringValue();
		switch (type) {
		case "XYZ":
			dest = new PDFDestination(destArray[0], XYZ);
			break;
		case "Fit":
			dest = new PDFDestination(destArray[0], FIT);
			break;
		case "FitH":
			dest = new PDFDestination(destArray[0], FITH);
			break;
		case "FitV":
			dest = new PDFDestination(destArray[0], FITV);
			break;
		case "FitR":
			dest = new PDFDestination(destArray[0], FITR);
			break;
		case "FitB":
			dest = new PDFDestination(destArray[0], FITB);
			break;
		case "FitBH":
			dest = new PDFDestination(destArray[0], FITBH);
			break;
		case "FitBV":
			dest = new PDFDestination(destArray[0], FITBV);
			break;
		default:
			throw new PDFParseException("Unknown destination type: " + type);
		}

		// now fill in the arguments based on the type
		switch (dest.getType()) {
		case XYZ:
			dest.setLeft(destArray[2].getFloatValue());
			dest.setTop(destArray[3].getFloatValue());
			dest.setZoom(destArray[4].getFloatValue());
			break;
		case FITH:
            case FITBV:
            case FITBH:
            case FITV:
                if (destArray.length > 2) {
				dest.setTop(destArray[2].getFloatValue());
			} else {
				dest.setTop(0.0F);
			}
			break;
            case FITR:
			dest.setLeft(destArray[2].getFloatValue());
			dest.setBottom(destArray[3].getFloatValue());
			dest.setRight(destArray[4].getFloatValue());
			dest.setTop(destArray[5].getFloatValue());
			break;
            default:
			break;
		}

		return dest;
	}

	/**
	 * Get the type of this destination
	 *
	 * @return a int.
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Get the PDF Page object associated with this destination
	 *
	 * @return a {@link org.loboevolution.pdfview.PDFObject} object.
	 */
	public PDFObject getPage() {
		return this.pageObj;
	}

	/**
	 * Get the left coordinate value
	 *
	 * @return a float.
	 */
	public float getLeft() {
		return this.left;
	}

	/**
	 * Set the left coordinate value
	 *
	 * @param left a float.
	 */
	public void setLeft(float left) {
		this.left = left;
	}

	/**
	 * Get the right coordinate value
	 *
	 * @return a float.
	 */
	public float getRight() {
		return this.right;
	}

	/**
	 * Set the right coordinate value
	 *
	 * @param right a float.
	 */
	public void setRight(float right) {
		this.right = right;
	}

	/**
	 * Get the top coordinate value
	 *
	 * @return a float.
	 */
	public float getTop() {
		return this.top;
	}

	/**
	 * Set the top coordinate value
	 *
	 * @param top a float.
	 */
	public void setTop(float top) {
		this.top = top;
	}

	/**
	 * Get the bottom coordinate value
	 *
	 * @return a float.
	 */
	public float getBottom() {
		return this.bottom;
	}

	/**
	 * Set the bottom coordinate value
	 *
	 * @param bottom a float.
	 */
	public void setBottom(float bottom) {
		this.bottom = bottom;
	}

	/**
	 * Get the zoom value
	 *
	 * @return a float.
	 */
	public float getZoom() {
		return this.zoom;
	}

	/**
	 * Set the zoom value
	 *
	 * @param zoom a float.
	 */
	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	/**
	 * Get a destination, given a name. This means the destination is in the
	 * root node's dests dictionary.
	 */
	private static PDFObject getDestFromName(PDFObject name, PDFObject root) throws IOException {
		// find the dests object in the root node
		PDFObject dests = root.getDictRef("Dests");
		if (dests != null) {
			// find this name in the dests dictionary
			return dests.getDictRef(name.getStringValue());
		}

		// not found
		return null;
	}

	/**
	 * Get a destination, given a string. This means the destination is in the
	 * root node's names dictionary.
	 */
	private static PDFObject getDestFromString(PDFObject str, PDFObject root) throws IOException {
		// find the names object in the root node
		PDFObject names = root.getDictRef("Names");
		if (names != null) {
			// find the dests entry in the names dictionary
			PDFObject dests = names.getDictRef("Dests");
			if (dests != null) {
				// create a name tree object
				NameTree tree = new NameTree(dests);

				// find the value we're looking for
				PDFObject obj = tree.find(str.getStringValue());

				// if we get back a dictionary, look for the /D value
				if (obj != null && obj.getType() == PDFObject.DICTIONARY) {
					obj = obj.getDictRef("D");
				}

				// found it
				return obj;
			}
		}

		// not found
		return null;
	}
}
