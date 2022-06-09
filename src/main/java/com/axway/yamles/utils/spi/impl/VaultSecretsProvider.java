package com.axway.yamles.utils.spi.impl;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.SecretsProviderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class VaultSecretsProvider extends AbstractSecretsProvider {
	private static final Logger log = LogManager.getLogger(VaultSecretsProvider.class);

	@ArgGroup(exclusive = false)
	VaultConfig config;

	public static class VaultConfig {
		@Option(names = "--vault-addr", required = true)
		String addr;
		@Option(names = "--vault-token", required = true)
		String token;
		@Option(names = "--vault-skip-verify", required = false)
		boolean skipVerify;
		@Option(names = "--vault-kv-path", required = true)
		String kvPath;
	}

	@Override
	public String getName() {
		return "vault";
	}

	@Override
	public Optional<String> getSecret(String key) {
		if (this.config == null)
			return Optional.empty();

		try {
			String[] parts = key.split(":");
			return getSecret(parts[0], parts[1]);
		} catch (Exception e) {
			log.error(e);
			return Optional.empty();
		}
	}

	Optional<String> getSecret(String kv, String field) throws Exception {
		try (CloseableHttpClient client = createClient()) {
			StringBuilder url = new StringBuilder();
			url.append(this.config.addr).append("/v1/").append(this.config.kvPath).append("/").append(kv);

			HttpGet request = new HttpGet(url.toString());
			request.addHeader("X-Vault-Token", this.config.token);

			try (CloseableHttpResponse response = client.execute(request)) {
				if (response.getCode() == 404) {
					return Optional.empty();
				}
				if (response.getCode() != 200) {
					throw new SecretsProviderException(this, "error on accessign vault");
				}
				ObjectMapper om = new ObjectMapper();
				JsonNode node = om.readTree(response.getEntity().getContent());

				JsonNode value = node.at("/data/data/" + field);
				return Optional.of(value.asText());
			}
		}
	}

	private CloseableHttpClient createClient()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslsf).register("http", new PlainConnectionSocketFactory()).build();

		BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(
				socketFactoryRegistry);
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

		return httpClient;
	}
}
