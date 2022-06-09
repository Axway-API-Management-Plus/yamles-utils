package com.axway.yamles.utils.spi.impl;

import java.io.IOException;
import java.util.Optional;

import com.axway.yamles.utils.spi.SecretsProvider;
import com.axway.yamles.utils.spi.SecretsProviderException;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;

public abstract class AbstractSecretsProvider implements SecretsProvider {

	@Override
	public Object apply(String context, Options options) throws IOException {
		Optional<String> secret = getSecret(context);
		if (!secret.isPresent()) {
			throw new SecretsProviderException(this, "secret not found: " + context);
		}

		return new Handlebars.SafeString(secret.get());
	}
}
