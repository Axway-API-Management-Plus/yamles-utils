package com.axway.yamles.utils.spi.impl;

import java.io.IOException;
import java.util.Optional;

import com.axway.yamles.utils.spi.LookupProvider;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;

public abstract class AbstractLookupProvider implements LookupProvider {

	@Override
	public Object apply(String key, Options options) throws IOException {
		Optional<String> value = lookup(key);
		if (!value.isPresent()) {
			throw new LookupProviderException(this, "lookup key not found: " + key);
		}

		return new Handlebars.SafeString(value.get());
	}
}
