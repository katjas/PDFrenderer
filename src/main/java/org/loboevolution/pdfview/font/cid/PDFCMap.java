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
package org.loboevolution.pdfview.font.cid;

import java.io.IOException;
import java.util.HashMap;

import org.loboevolution.pdfview.PDFDebugger;
import org.loboevolution.pdfview.PDFObject;

/**
 * A CMap maps from a character in a composite font to a font/glyph number
 * pair in a CID font.
 *
 * Author  jkaplan
  *
 */
public abstract class PDFCMap {
    /**
     * A cache of known CMaps by name
     */
    private static HashMap<String, PDFCMap> cache;
    
    /**
     * Creates a new instance of CMap
     */
    protected PDFCMap() {}
    
    /**
     * Get a CMap, given a PDF object containing one of the following:
     *  a string name of a known CMap
     *  a stream containing a CMap definition
     *
     * @param map a {@link org.loboevolution.pdfview.PDFObject} object.
     * @return a {@link org.loboevolution.pdfview.font.cid.PDFCMap} object.
     * @throws java.io.IOException if any.
     */
    public static PDFCMap getCMap(PDFObject map) throws IOException {
        if (map.getType() == PDFObject.NAME) {
            return getCMap(map.getStringValue());
        } else if (map.getType() == PDFObject.STREAM) {
            return parseCMap(map);
        } else {
            throw new IOException("CMap type not Name or Stream!");
        }
    }
       
    /**
     * Get a CMap, given a string name
     *
     * @param mapName a {@link java.lang.String} object.
     * @return a {@link org.loboevolution.pdfview.font.cid.PDFCMap} object.
     * @throws java.io.IOException if any.
     */
    public static PDFCMap getCMap(String mapName) throws IOException {
        if (cache == null) {
            populateCache();
        }
        
        if (!cache.containsKey(mapName)) {
            //throw new IOException("Unknown CMap: " + mapName);
        	PDFDebugger.debug("Unknown CMap: '" + mapName + "' procced with 'Identity-H'");
	       	return cache.get("Identity-H");
        }
            
        return cache.get(mapName);
    }
    
    /**
     * Populate the cache with well-known types
     */
    protected static void populateCache() {
        cache = new HashMap<>();
    
        // add the Identity-H map
        cache.put("Identity-H", new PDFCMap() {
            @Override
			public char map(char src) {
                return src;
            }
        });
    }
    
    /**
     * Parse a CMap from a CMap stream
     *
     * @param map a {@link org.loboevolution.pdfview.PDFObject} object.
     * @return a {@link org.loboevolution.pdfview.font.cid.PDFCMap} object.
     * @throws java.io.IOException if any.
     */
    protected static PDFCMap parseCMap(PDFObject map) throws IOException {
       	return new ToUnicodeMap(map);
    }
    
    /**
     * Map a given source character to a destination character
     *
     * @param src a char.
     * @return a char.
     */
    public abstract char map(char src);
    
    /**
     * Get the font number assoicated with a given source character
     *
     * @param src a char.
     * @return a int.
     */
    public int getFontID(char src) {
        return 0;
    }
    
}
