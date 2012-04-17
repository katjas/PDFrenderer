package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Roll implements PostScriptOperation {
	@Override
	public void eval(Stack<Object> environment) {
	    // <i>anyn-1 ... any0 n j</i> <b>roll</b> <i>any(j-1)mod n ... anyn-1 ... any</i>
	    // Roll n elements up j times
		long j = Math.round((Double)environment.pop());
		long n = Math.round((Double)environment.pop());
		Object[] topN = new Object[(int)n];
		for (int i=0; i < n; i++) topN[i] = environment.pop();
		for (int i=0; i < n; i++) {
			int k = (int)(i - j);
			if (k < 0) k += n;
			if (k >= n) k-= n;
			environment.push(topN[k]);
		}
	}
}
