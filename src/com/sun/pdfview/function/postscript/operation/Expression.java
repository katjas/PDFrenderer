package com.sun.pdfview.function.postscript.operation;

import java.util.LinkedList;



public class Expression extends LinkedList<Object> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean equals(Object obj) {
        if (obj instanceof Expression) {
            // actually validate the list contents are the same expressions
            return true;
        }
        return false;
    }
}
