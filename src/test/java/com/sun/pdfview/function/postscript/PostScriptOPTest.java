package com.sun.pdfview.function.postscript;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.junit.Test;

import com.sun.pdfview.function.postscript.operation.OperationSet;
import com.sun.pdfview.function.postscript.operation.PostScriptOperation;

public class PostScriptOPTest {

	public static Stack<Object> parse(String text) {
		Stack<Object> stack = new Stack<Object>();
		PostScriptParser p = new PostScriptParser();
		List<String> tokens = p.parse(text);
		for (Iterator<String> iterator = tokens.iterator(); iterator.hasNext(); ) {
			String token = iterator.next();
			PostScriptOperation op = OperationSet.getInstance().getOperation(token);
			op.eval(stack);
		}
		return stack;
	}

	
	@Test
	public void testRoll() {
		Stack<Object> stack = parse("1 2 3 4 5 5 -2 roll");
		assertEquals(2, ((Number)stack.pop()).intValue());
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertEquals(5, ((Number)stack.pop()).intValue());
		assertEquals(4, ((Number)stack.pop()).intValue());
		assertEquals(3, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());
		
		stack = parse("1 2 3 4 5 5 2 roll");
		assertEquals(3, ((Number)stack.pop()).intValue());
		assertEquals(2, ((Number)stack.pop()).intValue());
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertEquals(5, ((Number)stack.pop()).intValue());
		assertEquals(4, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());
		
		stack = parse("1 2 3 4 5 5 7 roll");
		assertEquals(3, ((Number)stack.pop()).intValue());
		assertEquals(2, ((Number)stack.pop()).intValue());
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertEquals(5, ((Number)stack.pop()).intValue());
		assertEquals(4, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());

		stack = parse("1 2 3 4 5 5 0 roll");
		assertEquals(5, ((Number)stack.pop()).intValue());
		assertEquals(4, ((Number)stack.pop()).intValue());
		assertEquals(3, ((Number)stack.pop()).intValue());
		assertEquals(2, ((Number)stack.pop()).intValue());
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());
	}
	
	@Test
	public void testIndex() {
		Stack<Object> stack = parse("1 0 index");
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());
		
		stack = parse("1 2 3 1 index");
		assertEquals(2, ((Number)stack.pop()).intValue());
		assertEquals(3, ((Number)stack.pop()).intValue());
	}
	
	@Test
	public void testExch() {
		Stack<Object> stack = parse("1 0 exch");
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertEquals(0, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());
		
		stack = parse("1 3.1 0 exch");
		assertEquals(3.1, ((Number)stack.pop()).doubleValue(), 1e-16);
		assertEquals(0, ((Number)stack.pop()).intValue());
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());
	}

	@Test
	public void testSub() {
		Stack<Object> stack = parse("1 2 sub");
		assertEquals(-1, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());		

		stack = parse("6.3 2 sub");
		assertEquals(4.3, ((Number)stack.pop()).doubleValue(), 1e-16);
		assertTrue(stack.isEmpty());
	}
	
	@Test
	public void testDup() {
		Stack<Object> stack = parse("1 2 dup");
		assertEquals(2, ((Number)stack.pop()).intValue());
		assertEquals(2, ((Number)stack.pop()).intValue());
		assertEquals(1, ((Number)stack.pop()).intValue());
		assertTrue(stack.isEmpty());		
	}
	
}
