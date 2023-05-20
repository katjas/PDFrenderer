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

package org.loboevolution.pdfview.font.ttf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p>NameTable class.</p>
 *
 * Author  jon
  *
 */
public class NameTable extends TrueTypeTable {
    /**
     * Values for platformID
     */
    public static final short PLATFORMID_UNICODE    = 0;
    /** Constant <code>PLATFORMID_MACINTOSH=1</code> */
    public static final short PLATFORMID_MACINTOSH  = 1;
    /** Constant <code>PLATFORMID_MICROSOFT=3</code> */
    public static final short PLATFORMID_MICROSOFT  = 3;
    
    /**
     * Values for platformSpecificID if platform is Mac
     */
    public static final short ENCODINGID_MAC_ROMAN = 0;
    
    /**
     * Values for platformSpecificID if platform is Unicode
     */
    public static final short ENCODINGID_UNICODE_DEFAULT = 0;
    /** Constant <code>ENCODINGID_UNICODE_V11=1</code> */
    public static final short ENCODINGID_UNICODE_V11     = 1;
    /** Constant <code>ENCODINGID_UNICODE_V2=3</code> */
    public static final short ENCODINGID_UNICODE_V2      = 3;
    
    /**
     * Values for language ID if platform is Mac
     */
    public static final short LANGUAGEID_MAC_ENGLISH     = 0;
    
    /**
     * Values for nameID
     */
    public static final short NAMEID_COPYRIGHT        = 0;
    /** Constant <code>NAMEID_FAMILY=1</code> */
    public static final short NAMEID_FAMILY           = 1;
    /** Constant <code>NAMEID_SUBFAMILY=2</code> */
    public static final short NAMEID_SUBFAMILY        = 2;
    /** Constant <code>NAMEID_SUBFAMILY_UNIQUE=3</code> */
    public static final short NAMEID_SUBFAMILY_UNIQUE = 3;
    /** Constant <code>NAMEID_FULL_NAME=4</code> */
    public static final short NAMEID_FULL_NAME        = 4;
    /** Constant <code>NAMEID_VERSION=5</code> */
    public static final short NAMEID_VERSION          = 5;
    /** Constant <code>NAMEID_POSTSCRIPT_NAME=6</code> */
    public static final short NAMEID_POSTSCRIPT_NAME  = 6;
    /** Constant <code>NAMEID_TRADEMARK=7</code> */
    public static final short NAMEID_TRADEMARK        = 7;
    /**
     * The format of this table
     */
    private short format;
    
    /**
     * The actual name records
     */
    private final SortedMap<NameRecord,String> records;
    
    
    /**
     * Creates a new instance of NameTable
     */
    protected NameTable() {
        super (TrueTypeTable.NAME_TABLE);
        
        this.records = Collections.synchronizedSortedMap(new TreeMap<>());
    }
    
    /**
     * Add a record to the table
     *
     * @param platformID a short.
     * @param platformSpecificID a short.
     * @param languageID a short.
     * @param nameID a short.
     * @param value a {@link java.lang.String} object.
     */
    public void addRecord(short platformID, short platformSpecificID,
                          short languageID, short nameID,
                          String value) {
        NameRecord rec = new NameRecord(platformID, platformSpecificID,
                                        languageID, nameID);
        this.records.put(rec, value);
    }
    
    /**
     * Get a record from the table
     *
     * @param platformID a short.
     * @param platformSpecificID a short.
     * @param languageID a short.
     * @param nameID a short.
     * @return a {@link java.lang.String} object.
     */
    public String getRecord(short platformID, short platformSpecificID,
                            short languageID, short nameID) {
    
        NameRecord rec = new NameRecord(platformID, platformSpecificID,
                                        languageID, nameID);
        return this.records.get(rec);
    }
    
    /**
     * Remove a record from the table
     *
     * @param platformID a short.
     * @param platformSpecificID a short.
     * @param languageID a short.
     * @param nameID a short.
     */
    public void removeRecord(short platformID, short platformSpecificID,
                             short languageID, short nameID) {
        NameRecord rec = new NameRecord(platformID, platformSpecificID,
                                        languageID, nameID);
        this.records.remove(rec);
    }
    
    /**
     * Determine if we have any records with a given platform ID
     *
     * @param platformID a short.
     * @return a boolean.
     */
    public boolean hasRecords(short platformID) {
        for (NameRecord rec : this.records.keySet()) {
            if (rec.platformID == platformID) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determine if we have any records with a given platform ID and
     * platform-specific ID
     *
     * @param platformID a short.
     * @param platformSpecificID a short.
     * @return a boolean.
     */
    public boolean hasRecords(short platformID, short platformSpecificID) {
        for (NameRecord rec : this.records.keySet()) {
            if (rec.platformID == platformID &&
                    rec.platformSpecificID == platformSpecificID) {
                return true;
            }
        }
        
        return false;
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * Read the table from data
	 */
    @Override
	public void setData(ByteBuffer data) {
        //read table header
        setFormat(data.getShort());
        int count = data.getShort();
        int stringOffset = data.getShort();
        
        // read the records
        for (int i = 0; i < count; i++) {
            short platformID = data.getShort();
            short platformSpecificID = data.getShort();
            short languageID = data.getShort();
            short nameID = data.getShort();
            
            int length = data.getShort() & 0xFFFF;
            int offset = data.getShort() & 0xFFFF;
            
            // read the String data
            data.mark();
            data.position(stringOffset + offset);
            
            ByteBuffer stringBuf = data.slice();
            stringBuf.limit(length);
            
            data.reset();
            
            // choose the character set
            String charsetName = getCharsetName(platformID, platformSpecificID);
            Charset charset = Charset.forName(charsetName);
            
            // parse the data as a string
            String value = charset.decode(stringBuf).toString();
        
            // add to the mix
            addRecord(platformID, platformSpecificID, languageID, nameID, value);
        }
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * Get the data in this table as a buffer
	 */
    @Override
	public ByteBuffer getData() {
        // alocate the output buffer
        ByteBuffer buf = ByteBuffer.allocate(getLength());
        
        // the start of string data
        short headerLength = (short) (6 + (12 * getCount()));
        
        // write the header
        buf.putShort(getFormat());
        buf.putShort(getCount());
        buf.putShort(headerLength);
        
        // the offset from the start of the strings table
        short curOffset = 0;
        
        // add the size of each record
        for (NameRecord rec : this.records.keySet()) {
            String value = this.records.get(rec);

            // choose the charset
            String charsetName = getCharsetName(rec.platformID,
                    rec.platformSpecificID);
            Charset charset = Charset.forName(charsetName);

            // encode
            ByteBuffer strBuf = charset.encode(value);
            short strLen = (short) (strBuf.remaining() & 0xFFFF);

            // write the IDs
            buf.putShort(rec.platformID);
            buf.putShort(rec.platformSpecificID);
            buf.putShort(rec.languageID);
            buf.putShort(rec.nameID);

            // write the size and offset
            buf.putShort(strLen);
            buf.putShort(curOffset);

            // remember or current position
            buf.mark();

            // move to the current offset and write the data
            buf.position(headerLength + curOffset);
            buf.put(strBuf);

            // reset stuff
            buf.reset();

            // increment offset
            curOffset += strLen;
        }
        
        // reset the pointer on the buffer
        buf.position(headerLength + curOffset);
        buf.flip();
        
        return buf;
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * Get the length of this table
	 */
    @Override
	public int getLength() {
        // start with the size of the fixed header plus the size of the
        // records
        int length = 6 + (12 * getCount());
        
        // add the size of each record
        for (NameRecord rec : this.records.keySet()) {
            String value = this.records.get(rec);

            // choose the charset
            String charsetName = getCharsetName(rec.platformID,
                    rec.platformSpecificID);
            Charset charset = Charset.forName(charsetName);

            // encode
            ByteBuffer buf = charset.encode(value);

            // add the size of the coded buffer
            length += buf.remaining();
        }
        
        return length;
    }
    
    /**
     * Get the format of this table
     *
     * @return a short.
     */
    public short getFormat() {
        return this.format;
    }
    
    /**
     * Set the format of this table
     *
     * @param format a short.
     */
    public void setFormat(short format) {
        this.format = format;
    }
    
    /**
     * Get the number of records in the table
     *
     * @return a short.
     */
    public short getCount() {
        return (short) this.records.size();
    }
    
    /**
     * Get the charset name for a given platform, encoding and language
     *
     * @param platformID a int.
     * @param encodingID a int.
     * @return a {@link java.lang.String} object.
     */
    public static String getCharsetName(int platformID, int encodingID) {
        String charset = "";   
            
        switch (platformID) {
            case PLATFORMID_UNICODE:
            case PLATFORMID_MICROSOFT:
                charset = "UTF-16";
                break;
            default:
            	charset = "US-ASCII";
                break;
        }
        
        return charset;
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * Get a pretty string
	 */
    @Override
	public String toString() {
        StringBuilder buf = new StringBuilder();
        String indent = "    ";
        
        buf.append(indent).append("Format: ").append(getFormat()).append("\n");
        buf.append(indent).append("Count : ").append(getCount()).append("\n");

        for (NameRecord rec : this.records.keySet()) {
            buf.append(indent).append(" platformID: ").append(rec.platformID);
            buf.append(" platformSpecificID: ").append(rec.platformSpecificID);
            buf.append(" languageID: ").append(rec.languageID);
            buf.append(" nameID: ").append(rec.nameID).append("\n");
            buf.append(indent).append("  ").append(this.records.get(rec)).append("\n");
        }
        
        return buf.toString();
    }
    
    /**
     * <p>getNames.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getNames()
    {
    	return Collections.unmodifiableCollection(records.values());
    }
    
    /**
     * A class to hold the data associated with each record
     */
    static class NameRecord implements Comparable {
        /**
         * Platform ID
         */
        final short platformID;
        
        /**
         * Platform Specific ID (Encoding)
         */
        final short platformSpecificID;
        
        /**
         * Language ID
         */
        final short languageID;
        
        /**
         * Name ID
         */
        final short nameID;
        
        /**
         * Create a new record
         */
        NameRecord(short platformID, short platformSpecificID,
                   short languageID, short nameID) {
            this.platformID = platformID;
            this.platformSpecificID = platformSpecificID;
            this.languageID = languageID;
            this.nameID = nameID;
        }
        
        
        /**
         * Compare two records
         */
        @Override
		public boolean equals(Object o) {
            return (compareTo(o) == 0);
        }
        
        /**
         * Compare two records
         */
        @Override
		public int compareTo(Object obj) {
            if (!(obj instanceof NameRecord)) {
                return -1;
            }
            
            NameRecord rec = (NameRecord) obj;
            
            if (this.platformID > rec.platformID) {
                return 1;
            } else if (this.platformID < rec.platformID) {
                return -1;
            } else if (this.platformSpecificID > rec.platformSpecificID) {
                return 1;
            } else if (this.platformSpecificID < rec.platformSpecificID) {
                return -1;
            } else if (this.languageID > rec.languageID) {
                return 1;
            } else if (this.languageID < rec.languageID) {
                return -1;
            } else if (this.nameID > rec.nameID) {
                return 1;
            } else if (this.nameID < rec.nameID) {
                return -1;
            } else {
                return 0;
            }
        }
        
        
    }
}
