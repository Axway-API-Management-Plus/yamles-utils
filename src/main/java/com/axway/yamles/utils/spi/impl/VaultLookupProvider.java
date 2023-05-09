package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.LookupProviderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class VaultLookupProvider extends AbstractLookupProvider {
	private static final Logger log = LogManager.getLogger(VaultLookupProvider.class);

	static class VaultToken {
		@Option(names = "--vault-token", description = "Vault token")
		String value;

		@Option(names = "--vault-token-file", description = "path to Vault token file")
		File file;
	}

	static class VaultClient {
		private final static Charset TOKEN_CHARSET = Charset.forName("ISO-8859-1");

		@Option(names = "--vault-addr", description = "Vault address [default: ${DEFAULT-VALUE}]", required = true, order = 0, paramLabel = "URL", defaultValue = "https://localhost:8200")
		String addr;

		@ArgGroup(exclusive = true, multiplicity = "1")
		VaultToken token;

		@Option(names = "--vault-kv-base", description = "base path the key/values (e.g., /kv/data/dev)", required = true, order = 1)
		String basePath;

		@Option(names = "--vault-skip-verify", description = "skip server name verification", required = false, order = 2)
		boolean skipVerify = false;

		Optional<String> getSecret(String path, String field) throws Exception {
			try (CloseableHttpClient client = createClient()) {
				URIBuilder uri = new URIBuilder(this.addr);
				uri.appendPath("/v1").appendPath(this.basePath).appendPath(path);

				if (log.isTraceEnabled()) {
					log.trace("vault lookup: {}", uri.toString());
				}

				HttpGet request = new HttpGet(uri.build());
				request.addHeader("X-Vault-Token", getToken());

				return client.execute(request, response -> {
					Optional<String> result = Optional.empty();
					if (response.getCode() == 404) {
						return result;
					}
					if (response.getCode() != 200) {
						throw new IOException("error on accessing vault (status=" + response.getCode() + ")");
					}
					ObjectMapper om = new ObjectMapper();
					JsonNode node = om.readTree(response.getEntity().getContent());

					JsonNode value = node.at("/data/data/" + field);
					if (!value.isMissingNode()) {
						result = Optional.of(value.asText());
					}
					return result;
				});
			}
		}

		private String getToken() throws IOException {
			String token = this.token.value;
			if (this.token.file != null) {
				byte[] b = Files.readAllBytes(this.token.file.toPath());
				token = new String(b, TOKEN_CHARSET);
			}
			return token;
		}

		private CloseableHttpClient createClient()
				throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
			CloseableHttpClient httpClient = null;

			if (this.skipVerify) {
				TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
				SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
						NoopHostnameVerifier.INSTANCE);

				Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
						.<ConnectionSocketFactory>create().register("https", sslsf)
						.register("http", new PlainConnectionSocketFactory()).build();

				BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(
						socketFactoryRegistry);
				httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
			} else {
				httpClient = HttpClients.createDefault();
			}

			return httpClient;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(this.addr).append('|').append(this.basePath);
			return str.toString();
		}
	}

	@ArgGroup(exclusive = false, multiplicity = "0..*")
	List<VaultClient> clients;

	@Override
	public String getName() {
		return "vault";
	}

	@Override
	public boolean isEnabled() {
		return this.clients != null && !this.clients.isEmpty();
	}

	@Override
	public void onRegistered() {
		for (VaultClient client : this.clients) {
			log.info("Vault lookup server registered: addr={}, basePath={}", client.addr, client.basePath);
		}
	}

	@Override
	public Optional<String> lookup(String key) {
		if (!isEnabled())
			return Optional.empty();

		try {
			String[] parts = key.split(":");
			if (parts.length != 2) {
				throw new LookupProviderException(this, "invalid Vault key: '" + key + "'");
			}
			String path = parts[0];
			String field = parts[1];

			if (log.isTraceEnabled()) {
				log.trace("vault lookup: path={}, field={}", path, field);
			}

			Optional<String> result = Optional.empty();

			for (VaultClient client : this.clients) {
				Optional<String> value = client.getSecret(path, field);
				if (value.isPresent()) {
					if (!result.isPresent()) {
						log.debug("found lookup key '{}' in {}", key, client);
					} else {
						log.debug("overwrite lookup key '{}' by {}", key, client);
					}
					result = value;
				}
			}

			return result;
		} catch (Exception e) {
			log.error(e);
			return Optional.empty();
		}
	}
}
