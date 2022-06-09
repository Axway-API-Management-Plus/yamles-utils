package com.axway.yamles.utils.lint.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RulesManager {
	
	private List<Rules> rules = new ArrayList<>();
	
	public RulesManager(List<File> rulesSources) {
		if (rulesSources == null) {
			return;
		}
		rulesSources.forEach(file -> {
			Rules r = Rules.loadRules(file);
			this.rules.add(r);
		});
	}
	
	public List<Rules> getRulesList() {
		return Collections.unmodifiableList(this.rules);
	}
}
