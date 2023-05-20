package org.loboevolution.pdfview.function.postscript.operation;

import java.util.Stack;

/**
 * <p>PostScriptOperation interface.</p>
 *
  *
  *
 */
public interface PostScriptOperation {

    /**
     * evaluate the function, popping the stack as needed and pushing results.
     *
     * @param environment a {@link java.util.Stack} object.
     */
    void eval(Stack<Object> environment);

}

