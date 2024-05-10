package com.axway.yamles.utils.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class EvalCommandTest {

	@Test
	void evaluateExpressionNoLineFeed() throws Exception {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(result);

		EvalCommand ec = new EvalCommand(out, "Hello World", false);
		ec.call();

		assertEquals("Hello World", result.toString());
	}
	
	@Test
	void evaluateExpressionWithLineFeed() throws Exception {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(result);

		EvalCommand ec = new EvalCommand(out, "Hello World", true);
		ec.call();

		assertEquals("Hello World" + System.lineSeparator(), result.toString());
	}
}
