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

package org.loboevolution.pdfview.decode;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFParseException;

/**
 * The abstract superclass of various predictor objects that undo well-known
 * prediction algorithms.
 *
  *
  *
 */
public abstract class Predictor {
    /** well known algorithms */
    public static final int TIFF = 0;
    /** Constant <code>PNG=1</code> */
    public static final int PNG = 1;
    
    /** the algorithm to use */
    private final int algorithm;
    
    /** the number of colors per sample */
    private int colors = 1;
    
    /** the number of bits per color component */
    private int bpc = 8;
    
    /** the number of columns per row */
    private int columns = 1;
    
    /**
     * Create an instance of a predictor.  Use <code>getPredictor()</code>
     * instead of this.
     *
     * @param algorithm a int.
     */
    protected Predictor(int algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Actually perform this algorithm on decoded image data.
     * Subclasses must implement this method
     *
     * @param imageData a {@link java.nio.ByteBuffer} object.
     * @return a {@link java.nio.ByteBuffer} object.
     * @throws java.io.IOException if any.
     */
    public abstract ByteBuffer unpredict(ByteBuffer imageData)
        throws IOException;
    
    /**
     * Get an instance of a predictor
     *
     * @param params the filter parameters
     * @return a {@link org.loboevolution.pdfview.decode.Predictor} object.
     * @throws java.io.IOException if any.
     */
    public static Predictor getPredictor(PDFObject params)
        throws IOException
    {
        // get the algorithm (required)
        PDFObject algorithmObj = params.getDictRef("Predictor");
        if (algorithmObj == null) {
            // no predictor
            return null;
        }
        int algorithm = algorithmObj.getIntValue();
    
        // create the predictor object
        Predictor predictor = null;
        switch (algorithm) {
            case 1:
                // no predictor
                return null;
            case 2:
            	predictor = new TIFFPredictor();
            break;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                predictor = new PNGPredictor();
                break;
            default:
                throw new PDFParseException("Unknown predictor: " + algorithm);
        }
        
        // read the colors (optional)
        PDFObject colorsObj = params.getDictRef("Colors");
        if (colorsObj != null) {
            predictor.setColors(colorsObj.getIntValue());
        }
        
        // read the bits per component (optional)
        PDFObject bpcObj = params.getDictRef("BitsPerComponent");
        if (bpcObj != null) {
            predictor.setBitsPerComponent(bpcObj.getIntValue());
        }
        
        // read the columns (optional)
        PDFObject columnsObj = params.getDictRef("Columns");
        if (columnsObj != null) {
            predictor.setColumns(columnsObj.getIntValue());
        }
        
        // all set
        return predictor;
    }
    
    /**
     * Get the algorithm in use
     *
     * @return one of the known algorithm types
     */
    public int getAlgorithm() {
        return this.algorithm;
    }
    
    /**
     * Get the number of colors per sample
     *
     * @return a int.
     */
    public int getColors() {
        return this.colors;
    }
    
    /**
     * Set the number of colors per sample
     *
     * @param colors a int.
     */
    protected void setColors(int colors) {
        this.colors = colors;
    }
    
    /**
     * Get the number of bits per color component
     *
     * @return a int.
     */
    public int getBitsPerComponent() {
        return this.bpc;
    }
    
    /**
     * Set the number of bits per color component
     *
     * @param bpc a int.
     */
    public void setBitsPerComponent(int bpc) {
        this.bpc = bpc;
    }
    
    /**
     * Get the number of columns
     *
     * @return a int.
     */
    public int getColumns() {
        return this.columns;
    }
    
    /**
     * Set the number of columns
     *
     * @param columns a int.
     */
    public void setColumns(int columns) {
        this.columns = columns;
    }
}
