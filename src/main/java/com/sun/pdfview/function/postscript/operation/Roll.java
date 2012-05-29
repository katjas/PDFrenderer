package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Roll implements PostScriptOperation {
	@Override
	public void eval(Stack<Object> environment) {
	    // <i>anyn-1 ... any0 n j</i> <b>roll</b> <i>any(j-1)mod n ... anyn-1 ... any</i>
	    // Roll n elements up j times
	    Object obj = environment.pop();
	    environment.push(obj);
	    environment.push(obj);
	}
}
