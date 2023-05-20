package org.loboevolution.pdfview.function.postscript.operation;

import java.util.Stack;



final class PushAsNumber implements PostScriptOperation {

	private final String token;
	
	/**
	 ***********************************************************************
	 * Constructor
	 *
	 * @param numberToken
	 ***********************************************************************
	 */
	public PushAsNumber(String numberToken) {
		super();
		this.token = numberToken;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 ***********************************************************************
	 * eval
	 * @see org.loboevolution.pdfview.function.postscript.operation.PostScriptOperation#eval(java.util.Stack)
	 ***********************************************************************
	 */
	@Override
	public void eval(Stack<Object> environment) {
		try {
			double number = Double.parseDouble(this.token);
			environment.push(number);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("PS token is not supported "+this.token); 
		}	}

}

