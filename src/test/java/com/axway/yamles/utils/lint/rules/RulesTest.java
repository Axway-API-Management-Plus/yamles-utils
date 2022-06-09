package com.axway.yamles.utils.lint.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class RulesTest {

	@Test
	void loadRules() throws Exception {
		String yaml = "---\n" //
				+ "rules:\n" //
				+ "  rule-1:\n" //
				+ "    name: Rule 1\n" //
				+ "    description: The first rule\n" //
				+ "    fileType: KPSPackage\n" //
				+ "    filePatterns:\n" //
				+ "      - 'Dir/File*'\n" //
				+ "      - 'Dir/SubDir/File*'\n" //
				+ "    assertions:\n" //
				+ "      - path: '$.hello'\n" //
				+ "        type: environmentalized\n" //
				+ "        message: Field must be environmentalized\n" //
				+ "      - path: '$.world'\n" //
				+ "        type: exists\n" //
				+ "  rule-2:\n" //
				+ "    name: Rule 2\n" //
				+ "    description: The second rule\n" //
				+ "    fileType: KPSPackage\n" //
				+ "    filePatterns:\n" //
				+ "      - 'Dir/File*'\n" //
				+ "      - 'Dir/SubDir/File*'\n" //
				+ "    assertions:\n" //
				+ "      - path: '$.hello'\n" //
				+ "        type: environmentalized\n" //
				+ "        message: Field must be environmentalized\n" //
				+ "      - path: '$.world'\n" //
				+ "        type: exists\n" //
				+ "      - path: '$.world'\n" //
				+ "        type: regex\n" //
				+ "        param: '.+'\n" //
				+ "...";

		Rules rules = Rules.loadRules(yaml);
		assertNotNull(rules);

		Map<String, Rule> rmap = rules.getRules();
		assertNotNull(rmap);

		assertEquals(2, rmap.size());

		Rule rule;

		rule = rmap.get("rule-1");
		assertNotNull(rule);
		assertEquals("Rule 1", rule.getName());
		assertEquals("The first rule", rule.getDescription());
		assertEquals(2, rule.getFilePatterns().size());
		assertEquals(2, rule.getAssertions().size());

		rule = rmap.get("rule-2");
		assertNotNull(rule);
		assertEquals("Rule 2", rule.getName());
		assertEquals("The second rule", rule.getDescription());
	}
}
