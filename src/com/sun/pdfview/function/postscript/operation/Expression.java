package com.sun.pdfview.function.postscript.operation;

import java.util.LinkedList;

public class Expression extends LinkedList<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Expression;
	}
}
