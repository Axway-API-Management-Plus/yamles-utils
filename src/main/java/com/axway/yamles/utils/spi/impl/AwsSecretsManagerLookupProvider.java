package com.axway.yamles.utils.spi.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.Command;

import com.axway.yamles.utils.helper.JsonDoc;
import com.axway.yamles.utils.spi.LookupProviderException;

import picocli.CommandLine.Option;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Command
public class AwsSecretsManagerLookupProvider extends AbstractJsonDocLookupProvider {

	private static final Logger log = LogManager.getLogger(AwsSecretsManagerLookupProvider.class);

	@Option(names = { "--lookup-aws-secret" }, description = "name of AWS secret containing keys", paramLabel = "NAME")
	private List<String> secretNames;

	public AwsSecretsManagerLookupProvider() {
		super(log);
	}

	@Override
	public String getName() {
		return "aws-sm";
	}

	@Override
	public boolean isEnabled() {
		return this.secretNames != null && this.secretNames.size() > 0;
	}

	@Override
	public void onRegistered() {
		synchronized (this) {
			if (!isEmpty())	return;
			
			SecretsManagerClient client = SecretsManagerClient.builder().build();

			for (String secretName : this.secretNames) {
				GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName)
						.build();

				GetSecretValueResponse getSecretValueResponse;

				try {
					getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
					String secret = getSecretValueResponse.secretString();
					JsonDoc doc = new JsonDoc(secretName, secret);
					add(doc);
					log.info("AWS Secrets Manager lookup registered: {}", doc.getName());					
				} catch (Exception e) {
					throw new LookupProviderException(this, "error on loading secret from AWS: " + secretName, e);
				}
			}
		}
	}
}
