package com.axway.yamles.utils.spi.impl;

import java.util.Optional;

import picocli.CommandLine.Command;

@Command
public class EnvSecretsProvider extends AbstractSecretsProvider {

	@Override
	public String getName() {
		return "env";
	}

	@Override
	public Optional<String> getSecret(String key) {
		if (key == null || key.isEmpty()) {
			return Optional.empty();
		}

		String secret = System.getenv(key);
		if (secret == null) {
			return Optional.empty();
		}
		return Optional.of(secret);
	}
}
