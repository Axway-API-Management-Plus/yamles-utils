package com.axway.yamles.utils.lint.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.lint.rules.Assertion.AssertionType;
import com.fasterxml.jackson.databind.node.ObjectNode;

class AssertionTest {

	@Test
	void test() throws Exception {
		String yamlStr = "---\n" //
				+ "hello:\n" //
				+ "  world:\n" //
				+ "    test: \"{{settings}}\"\n" //
				+ "  array:\n" //
				+ "    - key: k1\n" //
				+ "      value: v1\n" //
				+ "    - key: k2\n" //
				+ "      value: v2\n";

		ObjectNode yaml = (ObjectNode) Yaml.read(yamlStr);

		Assertion expr;

		expr = new Assertion("$.hello.world.test", AssertionType.environmentalized, null, "test");
		assertTrue(expr.check(yaml));

		expr = new Assertion("$.hello.world.test", AssertionType.exists, null, "test");
		assertTrue(expr.check(yaml));

		expr = new Assertion("$.hello.worldx", AssertionType.not_exists, null, "test");
		assertTrue(expr.check(yaml));

		expr = new Assertion("$.hello.world.test", AssertionType.regex, "\\{\\{.+}}", "test");
		assertTrue(expr.check(yaml));

		expr = new Assertion("$.hello.world.test", AssertionType.regex, "\\{\\{.}}", "test");
		assertFalse(expr.check(yaml));

		assertThrows(IllegalArgumentException.class, () -> {
			new Assertion("$.hello", AssertionType.regex, null, "test");
		});
	}
}
