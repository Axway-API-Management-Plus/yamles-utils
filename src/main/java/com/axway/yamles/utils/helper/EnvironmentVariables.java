package com.axway.yamles.utils.helper;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentVariables {

	private static final Map<String, String> customVars = new HashMap<>();

	public static void reset() {
		customVars.clear();
	}

	public static void put(String name, String value) {
		customVars.put(name, value);
	}

	public static void remove(String name) {
		customVars.remove(name);
	}

	public static String get(String name) {
		String value = customVars.get(name);
		if (value == null) {
			value = System.getenv(name);
		}
		return value;
	}
}
