package com.axway.yamles.utils.spi.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.axway.yamles.utils.spi.LookupProvider;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public abstract class AbstractLookupProvider implements LookupProvider {

	@Override
	public List<String> getArgumentNames() {
		return Arrays.asList("key");
	}

	@Override
	public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
		if (!isEnabled()) {
			throw new LookupProviderException(this, "lookup provider disabled");
		}

		String key = (String) args.get("key");
		Optional<String> value = lookup(key);
		if (!value.isPresent()) {
			throw new LookupProviderException(this, "lookup key not found: " + key);
		}

		return value.get();
	}
}
