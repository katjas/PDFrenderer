package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Index implements PostScriptOperation {
	@Override
	public void eval(Stack<Object> environment) {   // <i>anyn ... any0 n</i> <b>index</b> <i>anyn ... any0 anyn</i>
	    Object obj = environment.pop();
	    environment.push(obj);
	    environment.push(obj);
	}
}
