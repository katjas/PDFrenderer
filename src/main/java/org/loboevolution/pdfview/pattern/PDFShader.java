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

package org.loboevolution.pdfview.pattern;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFPaint;
import org.loboevolution.pdfview.PDFParseException;
import org.loboevolution.pdfview.colorspace.PDFColorSpace;

/**
 *
 * <p>A PDFShader fills a given region with a shading, such as a gradient.</p>
 *
 * Shading Dictionaries (section 4.6)<br>
 * A shading dictionary specifies details of a particular gradient fill,
 * including the type of shading to be used, the geometry of the area to
 * be shaded, and the geometry of the gradient fill. Various shading types
 * are available, depending on the value of the dictionary’s ShadingType entry:
 *
 * <p>Function-based shadings (type 1) define the color of every point in the
 * domain using a mathematical function (not necessarily smooth or continuous).</p>
 *
 * <p>Axial shadings (type 2) define a color blend along a pne between two
 * points, optionally extended beyond the boundary points by continuing
 * the boundary colors.</p>
 *
 * <p>Radial shadings (type 3) define a blend between two circles,
 * optionally extended beyond the boundary circles by continuing the
 * boundary colors. This type of shading is commonly used to represent
 * three-dimensional spheres and cones.</p>
 *
 * <p>Free-form Gouraud-shaded triangle meshes (type 4) define a
 * common construct used by many three-dimensional apppcations to
 * represent complex colored and shaded shapes. Vertices are specified
 * in free-form geometry.</p>
 *
 * <p>Lattice-form Gouraud-shaded triangle meshes (type 5) are based on
 * the same geometrical construct as type 4 but with vertices specified
 * as a pseudorectangular lattice.</p>
 *
 * <p>Coons patch meshes (type 6) construct a shading from one or more
 * color patches, each bounded by four cubic Bézier curves.</p>
 *
 * <p>Tensor-product patch meshes (type 7) are similar to type 6 but
 * with additional control points in each patch, affording greater
 * control over color mapping.</p>
 *
 * Table 4.28 shows the entries that all shading dictionaries share
 * in common; entries specific to particular shading types are
 * described in the relevant sections below.
 */
public abstract class PDFShader {
    
    /** Constant <code>FUNCTION_SHADING=1</code> */
    public static final  int             FUNCTION_SHADING = 1;
    /** Constant <code>AXIAL_SHADING=2</code> */
    public static final  int             AXIAL_SHADING = 2;
    /** Constant <code>RADIAL_SHADING=3</code> */
    public static final  int             RADIAL_SHADING = 3;
    /** Constant <code>FREE_FORM_SHADING=4</code> */
    public static final  int             FREE_FORM_SHADING = 4;
    /** Constant <code>LATTICE_SHADING=5</code> */
    public static final  int             LATTICE_SHADING = 5;
    /** Constant <code>COONS_PATCH_MESH_SHADING=6</code> */
    public static final  int             COONS_PATCH_MESH_SHADING = 6;
    /** Constant <code>TENSOR_PRODUCTS_MESH_SHADING=7</code> */
    public static final  int             TENSOR_PRODUCTS_MESH_SHADING = 7;

    /** The tolerance for reevaluating the shading function again */
    public static final float TOLERANCE = 1e-4f;

    /** the type of the shading (1 through 7)*/
    private final int type;
    
    /** the colorspace */
    private PDFColorSpace colorSpace;
    
    /** the background color */
    private PDFPaint background;
    
    /** the bounding box of the pattern */
    private Rectangle2D bbox;
    
    /**
     * Creates a new instance of PDFShader
     *
     * @param type a int.
     */
    protected PDFShader(int type) {
        this.type = type;
    }
    
    
    /**
     * Parse a pdf shader into a shader object
     *
     * @param shaderObj a {@link org.loboevolution.pdfview.PDFObject} object.
     * @param resources a {@link java.util.Map} object.
     * @return a {@link org.loboevolution.pdfview.pattern.PDFShader} object.
     * @throws java.io.IOException if any.
     */
    public static PDFShader getShader(PDFObject shaderObj, Map resources)
        throws IOException
    {
        // first see if the shader is already cached
        PDFShader shader = (PDFShader) shaderObj.getCache();
        if (shader != null) {
            return shader;
        }
        
        // read the type (required)
        PDFObject typeObj = shaderObj.getDictRef("ShadingType");
        if (typeObj == null) {
            throw new PDFParseException("No shader type defined!");
        }
        int type = typeObj.getIntValue();
        
        // create the shader
        switch (type) {
            case AXIAL_SHADING:
                shader = new ShaderType2();
                break;
    
            case RADIAL_SHADING:
            	shader = new ShaderType3();
            	break;

            case FUNCTION_SHADING:
            case FREE_FORM_SHADING:
            case LATTICE_SHADING:
            case COONS_PATCH_MESH_SHADING:
            case TENSOR_PRODUCTS_MESH_SHADING:
            default:    
            		shader = new DummyShader(type);
        }
        
        // read the color space (required)
        PDFObject csObj = shaderObj.getDictRef("ColorSpace");
        if (csObj == null) {
            throw new PDFParseException("No colorspace defined!");
        }
        PDFColorSpace cs = PDFColorSpace.getColorSpace(csObj, resources);
        shader.setColorSpace(cs);
        
        // read the background color (optional)
        PDFObject bgObj = shaderObj.getDictRef("Background");
        if (bgObj != null) {
            PDFObject[] bgObjs = bgObj.getArray();
            float[] bgArray = new float[bgObjs.length];
            for (int i = 0; i < bgArray.length; i++) {
                bgArray[i] = bgObjs[i].getFloatValue();
            }
            PDFPaint paint = cs.getPaint(bgArray);
            shader.setBackground(paint);          
        }
        
        // read the bounding box (optional)
        PDFObject bboxObj = shaderObj.getDictRef("BBox");
        if (bboxObj != null) {
            PDFObject[] rectObj = bboxObj.getArray();
            float minX = rectObj[0].getFloatValue();
            float minY = rectObj[1].getFloatValue();
            float maxX = rectObj[2].getFloatValue();
            float maxY = rectObj[3].getFloatValue();
            
            Rectangle2D bbox = 
                new Rectangle2D.Float(minX, minY,  maxX - minX, maxY - minY);
            shader.setBBox(bbox);
        }
        
        // parse the shader-specific attributes
        shader.parse(shaderObj);
        
        // set the cache
        shaderObj.setCache(shader);
        
        return shader;
    }
    
    /**
     * Get the type
     *
     * @return a int.
     */
    public int getType() {
        return this.type;
    }
    
    /**
     * Get the color space
     *
     * @return a {@link org.loboevolution.pdfview.colorspace.PDFColorSpace} object.
     */
    public PDFColorSpace getColorSpace() {
        return this.colorSpace;
    }

    /**
     * Set the color space
     *
     * @param colorSpace a {@link org.loboevolution.pdfview.colorspace.PDFColorSpace} object.
     */
    protected void setColorSpace(PDFColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }
    
    /**
     * Get the background color
     *
     * @return a {@link org.loboevolution.pdfview.PDFPaint} object.
     */
    public PDFPaint getBackground() {
        return this.background;
    }
    
    /**
     * Set the background color
     *
     * @param background a {@link org.loboevolution.pdfview.PDFPaint} object.
     */
    protected void setBackground(PDFPaint background) {
        this.background = background;
    }
    
    /**
     * Get the bounding box
     *
     * @return a {@link java.awt.geom.Rectangle2D} object.
     */
    public Rectangle2D getBBox() {
        return this.bbox;
    }
    
    /**
     * Set the bounding box
     *
     * @param bbox a {@link java.awt.geom.Rectangle2D} object.
     */
    protected void setBBox(Rectangle2D bbox) {
        this.bbox = bbox;
    }
    
    /**
     * Parse the shader-specific data
     *
     * @param shareObj a {@link org.loboevolution.pdfview.PDFObject} object.
     * @throws java.io.IOException if any.
     */
    public abstract void parse(PDFObject shareObj) throws IOException;
    
    /**
     * Returns paint that represents the selected shader
     *
     * @return a {@link org.loboevolution.pdfview.PDFPaint} object.
     */
    public abstract PDFPaint getPaint();
}
