package com.axway.yamles.utils.spi.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.Command;

import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.ConfigParameter.Type;
import com.axway.yamles.utils.spi.LookupDoc;
import com.axway.yamles.utils.spi.LookupFunction;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Command
public class AwsSecretsManagerJsonLookupProvider extends AbstractLookupDocLookupProvider {
	public static final ConfigParameter CFG_PARAM_SECRET = new ConfigParameter("secret_name", true, "Secret name",
			Type.string, false);
	public static final ConfigParameter CFG_PARAM_REGION = new ConfigParameter("region", false, "Region name",
			Type.string, false);

	private static final Logger log = LogManager.getLogger(AwsSecretsManagerJsonLookupProvider.class);

	public AwsSecretsManagerJsonLookupProvider() {
		super();
		add(CFG_PARAM_SECRET, CFG_PARAM_REGION);
	}

	@Override
	public String getName() {
		return "aws_sm_json";
	}

	@Override
	public String getSummary() {
		return "Lookup values from AWS Secrets Manager (secrets in JSON format).";
	}

	@Override
	public String getDescription() {
		return "The key represents the JSON Pointer to the property containing the value (e.g. '/user_password').";
	}

	@Override
	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException {
		String secretName = source.getConfig(CFG_PARAM_SECRET, "");
		String region = source.getConfig(CFG_PARAM_REGION, "");

		SecretsManagerClientBuilder builder = SecretsManagerClient.builder();
		if (!region.isEmpty()) {
			builder.region(Region.of(region));
		}
		SecretsManagerClient client = builder.build();

		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

		GetSecretValueResponse getSecretValueResponse;

		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
			String secret = getSecretValueResponse.secretString();
			LookupDoc doc = LookupDoc.fromJsonString(secret, secretName);
			return new LF(source.getAlias(), this, source.getConfigSource(), doc, log);
		} catch (Exception e) {
			throw new LookupProviderException(this, "error on loading secret from AWS: " + secretName, e);
		}
	}
}
