package com.axway.yamles.utils.spi.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.ConfigParameter.Type;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class AwsSecretsManagerPlainTextLookupProvider extends AbstractLookupProvider {
	private static class SMClient {
		private final String prefix;
		private final SecretsManagerClient client;

		public SMClient(String prefix, SecretsManagerClient client) {
			this.prefix = prefix == null ? "" : prefix;
			this.client = Objects.requireNonNull(client, "Secrets Manager client required");
		}

		public Optional<String> getPlaintext(String secretName) {
			secretName = this.prefix + secretName;

			Optional<String> result = Optional.empty();

			GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
			GetSecretValueResponse getSecretValueResponse;

			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
			result = Optional.ofNullable(getSecretValueResponse.secretString());

			return result;
		}
	}

	public static final ConfigParameter CFG_PARAM_PREFIX = new ConfigParameter("prefix", false,
			"Prefix for secret name", Type.string, false);

	public static final ConfigParameter CFG_PARAM_REGION = new ConfigParameter("region", false, "Region name",
			Type.string, false);

	private static final Logger log = LogManager.getLogger(AwsSecretsManagerPlainTextLookupProvider.class);

	private final Map<String, SMClient> clients = new HashMap<>();

	public AwsSecretsManagerPlainTextLookupProvider() {
		super("Secret name", EMPTY_FUNC_ARGS, new ConfigParameter[] { CFG_PARAM_PREFIX, CFG_PARAM_REGION });
	}

	@Override
	public String getName() {
		return "aws_sm_plain";
	}

	@Override
	public String getSummary() {
		return "Lookup plain text secrets from AWS Secrets Manager.";
	}

	@Override
	public String getDescription() {
		return "The key represents the name of the secret.";
	}

	@Override
	public boolean isEnabled() {
		return !this.clients.isEmpty();
	}

	@Override
	public void addSource(LookupSource source) throws LookupProviderException {
		if (this.clients.containsKey(source.getAlias())) {
			throw new LookupProviderException(this,
					"lookup already registered: provider=" + getName() + "; alias=" + source.getAlias());
		}
		
		String region = source.getConfig(CFG_PARAM_REGION, "");
		String prefix = source.getConfig(CFG_PARAM_PREFIX, "");

		SecretsManagerClientBuilder builder = SecretsManagerClient.builder();
		if (!region.isEmpty()) {
			builder.region(Region.of(region));
		}
		SMClient client = new SMClient(prefix, builder.build());

		this.clients.put(source.getAlias(), client);
	}

	@Override
	public Optional<String> lookup(String alias, Map<String, Object> args) {
		Optional<String> result = Optional.empty();
		SMClient client = this.clients.get(alias);
		if (client == null) {
			log.error("alias not found by provider: provider={}; alias={}", getName(), alias);
			return result;
		}

		String secretName = getArg(ARG_KEY, args, null);

		try {
			result = client.getPlaintext(secretName);
		} catch (Exception e) {
			throw new LookupProviderException(this, "error on loading secret from AWS: " + secretName, e);
		}
		return result;
	}
}
