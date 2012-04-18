package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Roll implements PostScriptOperation {
	@Override
	public void eval(Stack<Object> environment) {
	    // <i>anyn-1 ... any0 n j</i> <b>roll</b> <i>any(j-1)mod n ... anyn-1 ... any</i>
	    // Roll n elements up j times
		int j = ((Number)environment.pop()).intValue();
		int n = ((Number)environment.pop()).intValue();
		Object[] topN = new Object[(int)n];
		for (int i=0; i < n; i++) topN[i] = environment.pop();
		
		for (int i=0; i < n; i++) {
			int k = (-i + j - 1) % n;
			if (k < 0) k += n;
			environment.push(topN[k]);
		}
	}
}
