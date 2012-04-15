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

package com.sun.pdfview.pattern;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.PDFRenderer;

/**
 * A type 1 (tiling) pattern
 */
public class PatternType2 extends PDFPattern {
        
    /** the shader */
    public PDFShader shader;
        
    /** Creates a new instance of PatternType1 */
    public PatternType2() {
        super(2);
    }
    
    /**
     * Parse the pattern from the PDFObject
     *
     * Note the resources passed in are ignored...
     */
    @Override
	protected void parse(PDFObject patternObj, Map rsrc) throws IOException
    {
        this.shader = PDFShader.getShader(patternObj.getDictRef("Shading"), rsrc);        
    }
    
    /** 
     * Create a PDFPaint from this pattern and set of components.  
     * This creates a buffered image of this pattern using
     * the given paint, then uses that image to create the correct 
     * TexturePaint to use in the PDFPaint.
     *
     * @param basePaint the base paint to use, or null if not needed
     */
    @Override
	public PDFPaint getPaint(PDFPaint basePaint) {
    	return new TilingPatternPaint(shader.getPaint().getPaint(), this);
    }
    
    /** 
     * This class overrides PDFPaint to paint in the pattern coordinate space
     */
    static class TilingPatternPaint extends PDFPaint {
        /** the pattern to paint */
        private PatternType2 pattern;
        
        /** Create a tiling pattern paint */
        public TilingPatternPaint(Paint paint, PatternType2 pattern) {
            super (paint);
            
            this.pattern = pattern;
        }
        
        /**
         * fill a path with the paint, and record the dirty area.
         * @param state the current graphics state
         * @param g the graphics into which to draw
         * @param s the path to fill
         * @param drawn a Rectangle2D into which the dirty area (area drawn)
         * will be added.
         */
        @Override
		public Rectangle2D fill(PDFRenderer state, Graphics2D g,
                                GeneralPath s) {                        
            // first transform s into device space
            AffineTransform at = g.getTransform();
            Shape xformed = s.createTransformedShape(at);
                                    
            // push the graphics state so we can restore it
            state.push();
            
            // set the transform to be the inital transform concatentated
            // with the pattern matrix
            state.setTransform(state.getInitialTransform());
            state.transform(this.pattern.getTransform());
            
            // now figure out where the shape should be
            try {
                at = state.getTransform().createInverse();
            } catch (NoninvertibleTransformException nte) {
                // oh well (?)
            }
            xformed = at.createTransformedShape(xformed);
            
            if (pattern.shader.getBackground() != null) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g.setPaint(pattern.shader.getBackground().getPaint());
                g.fill(xformed);            	
            }
            // set the paint and draw the xformed shape
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setPaint(getPaint());
            g.fill(xformed);
            
            // restore the graphics state
            state.pop();
            
            // return the area changed
            return s.createTransformedShape(g.getTransform()).getBounds2D();
        }
    }

}