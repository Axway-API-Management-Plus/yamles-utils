package com.axway.yamles.utils.spi;

import java.nio.charset.Charset;
import java.util.Optional;

import com.github.jknack.handlebars.Helper;

public interface SecretsProvider extends Helper<String> {

	public static final Charset SECRETS_ENCODING = Charset.forName("UTF-8");

	public String getName();

	public Optional<String> getSecret(String key);

	public default Optional<byte[]> getSecretBytes(String key) {
		Optional<String> secret = getSecret(key);
		if (secret.isPresent()) {
			return Optional.of(secret.get().getBytes(SECRETS_ENCODING));
		}
		return Optional.empty();
	}
}
