package com.axway.yamles.utils.lint.rules;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePattern {
	private final Pattern pattern;

	public FilePattern(String pattern) {
		this.pattern = Pattern.compile(Objects.requireNonNull(pattern));
	}

	public boolean match(String projectFile) {
		Matcher m = this.pattern.matcher(projectFile);
		return m.matches();
	}

	@Override
	public String toString() {
		return this.pattern.toString();
	}

	@Override
	public int hashCode() {
		return this.pattern.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.pattern.equals(obj);
	}
}
