package org.loboevolution.pdfview.function.postscript.operation;

import java.util.LinkedList;



/**
 * <p>Expression class.</p>
 *
  *
  *
 */
public class Expression extends LinkedList<Object> {

	/** {@inheritDoc} */
    @Override
	public boolean equals(final Object obj) {
        // actually validate the list contents are the same expressions
        return obj instanceof Expression;
    }
}
