package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.util.Optional;

import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.spi.SecretsProviderException;
import com.fasterxml.jackson.databind.JsonNode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class InsecureSecretsProvider extends AbstractSecretsProvider {

	@Option(names = {
			"--secrets-insecure" }, description = "Path to an unencrypted YAML secrets file", paramLabel = "FILE")
	private File secretsFile;
	private JsonNode secrets;

	@Override
	public String getName() {
		return "insecure";
	}

	@Override
	public Optional<String> getSecret(String key) {
		if (this.secretsFile == null || key == null || key.trim().isEmpty())
			return Optional.empty();

		synchronized (this) {
			if (this.secrets == null) {
				try {
					this.secrets = Yaml.load(secretsFile);
				} catch (Exception e) {
					throw new SecretsProviderException(this, "error on loading secrets file", e);
				}
			}
		}

		JsonNode secret = this.secrets.at(key);
		if (secret == null || secret.isNull() || !secret.isTextual())
			return Optional.empty();

		String value = secret.asText();

		return Optional.of(value);
	}
}
