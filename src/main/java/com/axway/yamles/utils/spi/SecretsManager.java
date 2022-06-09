package com.axway.yamles.utils.spi;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

public class SecretsManager implements Helper<String> {
	private static final Logger log = LogManager.getLogger(SecretsManager.class);

	private static final SecretsManager instance = new SecretsManager();
	
	private final ServiceLoader<SecretsProvider> secretsProviders;
	
	public static SecretsManager getInstance() {
		return instance;
	}
	
	private SecretsManager() {
		this.secretsProviders = ServiceLoader.load(SecretsProvider.class);
		if (log.isDebugEnabled()) {
			this.secretsProviders.forEach(p -> {
				log.debug("secrets provider found: {}", p.getName());
			});
		}
	}
	
	public Iterator<SecretsProvider> getProviders() {
		return this.secretsProviders.iterator();
	}

	public String getSecret(String key) {
		Optional<String> secret = Optional.empty();
		Iterator<SecretsProvider> iter = secretsProviders.iterator();
		while (iter.hasNext()) {
			SecretsProvider sp = iter.next(); 
			secret = sp.getSecret(key);
			if (secret.isPresent()) {
				log.info("secret '{}' provided by '{}'", key, sp.getName());
				break;
			}
		}
		return secret.orElseThrow(() -> new SecretsManagerException("secret not found"));
	}
	
	public byte[] getSecretBytes(String key) {
		Optional<byte[]> secret = Optional.empty();
		Iterator<SecretsProvider> iter = secretsProviders.iterator();
		while (iter.hasNext()) {
			SecretsProvider sp = iter.next(); 
			secret = sp.getSecretBytes(key);
			if (secret.isPresent()) {
				log.info("secret '{}' found in provider '{}'", key, sp.getName());
				break;
			}
		}
		return secret.orElseThrow(() -> new SecretsManagerException("secret not found"));
	}
	

	@Override
	public Object apply(String context, Options options) throws IOException {
		return new Handlebars.SafeString(getSecret(context));
	}
}
