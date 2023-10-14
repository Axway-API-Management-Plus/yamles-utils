package com.axway.yamles.utils.plugins.aws;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.axway.yamles.utils.plugins.AbstractLookupProvider;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.LookupProviderException;
import com.axway.yamles.utils.plugins.LookupSource;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class AwsSecretsManagerPlainTextLookupProvider extends AbstractLookupProvider {
	public static final FunctionArgument ARG_KEY = new FunctionArgument("key", true, "Secret name");

	public static final ConfigParameter CFG_PARAM_PREFIX = new ConfigParameter("prefix", false,
			"Prefix for secret name", Type.string, false);

	public static final ConfigParameter CFG_PARAM_REGION = new ConfigParameter("region", false, "Region name",
			Type.string, false);

	private static class SMClient {
		private final String prefix;
		private final SecretsManagerClient client;

		public SMClient(String prefix, SecretsManagerClient client) {
			this.prefix = prefix == null ? "" : prefix;
			this.client = Objects.requireNonNull(client, "Secrets Manager client required");
		}

		public String getFullSecretName(String secretName) {
			return this.prefix + secretName;
		}

		public Optional<String> getPlaintext(String secretName) {
			secretName = getFullSecretName(secretName);

			Optional<String> result = Optional.empty();

			GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
			GetSecretValueResponse getSecretValueResponse;

			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

			SdkBytes binary = getSecretValueResponse.secretBinary();
			if (binary != null) {
				result = Optional.of(Base64.getEncoder().encodeToString(binary.asByteArray()));
			} else {
				result = Optional.ofNullable(getSecretValueResponse.secretString());
			}
			return result;
		}
	}

	static class LF extends LookupFunction {
		private final SMClient client;

		public LF(String alias, AwsSecretsManagerPlainTextLookupProvider provider, Optional<String> source,
				SMClient client) {
			super(alias, provider, source);
			this.client = Objects.requireNonNull(client, "Secrets Manager client required");
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			Optional<String> result = Optional.empty();

			String secretName = getArg(ARG_KEY, args, null);

			try {
				result = this.client.getPlaintext(secretName);
			} catch (Exception e) {
				throw new LookupFunctionException(this,
						"error on loading secret from AWS: " + this.client.getFullSecretName(secretName), e);
			}
			return result;
		}
	}

	public AwsSecretsManagerPlainTextLookupProvider() {
		super();
		add(ARG_KEY);
		add(CFG_PARAM_PREFIX, CFG_PARAM_REGION);
	}

	@Override
	public String getName() {
		return "aws_sm_plain";
	}

	@Override
	public String getSummary() {
		return "Lookup plaintext secrets from AWS Secrets Manager.";
	}

	@Override
	public String getDescription() {
		return "The key represents the name of the secret.";
	}

	@Override
	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException {
		// get configuration parameters
		String region = source.getConfig(CFG_PARAM_REGION, "");
		String prefix = source.getConfig(CFG_PARAM_PREFIX, "");

		Optional<LookupFunction> clf = checkOnlyLookupFunction(source);
		if (clf.isPresent())
			return clf.get();

		SecretsManagerClientBuilder builder = SecretsManagerClient.builder();
		if (!region.isEmpty()) {
			builder.region(Region.of(region));
		}
		SMClient client = new SMClient(prefix, builder.build());

		return new LF(source.getAlias(), this, source.getConfigSource(), client);
	}
}
