package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VaultLookupProvider extends AbstractLookupProvider {
	public static final FunctionArgument ARG_FIELD = new FunctionArgument("field", true, "");

	public static final ConfigParameter CFG_PARAM_ADDR = new ConfigParameter("addr", false, "",
			ConfigParameter.Type.string);
	public static final ConfigParameter CFG_PARAM_KV_BASE = new ConfigParameter("kv_base", true, "",
			ConfigParameter.Type.string);
	public static final ConfigParameter CFG_PARAM_SKIP_VERIFY = new ConfigParameter("skip_verify", false, "",
			ConfigParameter.Type.string);
	public static final ConfigParameter CFG_PARAM_TOKEN = new ConfigParameter("token", false, "",
			ConfigParameter.Type.string);
	public static final ConfigParameter CFG_PARAM_TOKEN_FILE = new ConfigParameter("token_file", false, "",
			ConfigParameter.Type.file);

	private static final Logger log = LogManager.getLogger(VaultLookupProvider.class);

	static class VaultToken {
		private final static Charset TOKEN_CHARSET = Charset.forName("ISO-8859-1");

		private String value;
		private File file;

		VaultToken(String token) {
			this.value = Objects.requireNonNull(token);
			this.file = null;
		}

		VaultToken(File tokenFile) {
			this.value = null;
			this.file = Objects.requireNonNull(tokenFile);
		}

		String getToken() throws IOException {
			String token = this.value;
			if (this.file != null) {
				byte[] b = Files.readAllBytes(this.file.toPath());
				token = new String(b, TOKEN_CHARSET);
			}
			return token;
		}
	}

	static class VaultClient {
		final String alias;
		final VaultToken token;
		final String basePath;
		final String addr;
		final boolean skipVerify;

		VaultClient(String alias, VaultToken token, String basePath, Optional<String> addr, boolean skipVerify) {
			this.alias = Objects.requireNonNull(alias);
			this.token = Objects.requireNonNull(token);
			this.basePath = Objects.requireNonNull(basePath);
			this.addr = Objects.requireNonNull(addr).isPresent() ? addr.get() : "https://localhost:8200";
			this.skipVerify = skipVerify;
		}

		Optional<String> getSecret(String path, String field) throws Exception {
			try (CloseableHttpClient client = createClient()) {
				URIBuilder uri = new URIBuilder(this.addr);
				uri.appendPath("/v1").appendPath(this.basePath).appendPath(path);

				if (log.isTraceEnabled()) {
					log.trace("vault lookup: {}", uri.toString());
				}

				HttpGet request = new HttpGet(uri.build());
				request.addHeader("X-Vault-Token", this.token.getToken());

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
			str.append("alias=").append(this.alias);
			str.append("; ");
			str.append("addr=").append(this.addr);
			str.append("; ");
			str.append("basePath=").append(this.basePath);
			return str.toString();
		}
	}

	Map<String, VaultClient> clients = new HashMap<>();

	public VaultLookupProvider() {
		super("path to KV");
		add(ARG_FIELD);
		add(CFG_PARAM_TOKEN, CFG_PARAM_TOKEN_FILE, CFG_PARAM_ADDR, CFG_PARAM_KV_BASE);
	}

	@Override
	public String getName() {
		return "vault";
	}

	@Override
	public String getSummary() {
		return "Lookup values from a Hashicorp Vault Key/Value store.";
	}
	
	@Override
	public String getDescription() {
		return "The key represents the path to the KV entry.";
	}

	@Override
	public void addSource(LookupSource source) throws LookupProviderException {
		String kvBase = source.getRequiredParam(CFG_PARAM_KV_BASE.getName());
		Optional<String> tokenStr = source.getParam(CFG_PARAM_TOKEN.getName());
		Optional<String> addr = source.getParam(CFG_PARAM_ADDR.getName());
		boolean skipVerify = source.getParam(CFG_PARAM_SKIP_VERIFY.getName()).orElse("false").equals("true");

		VaultToken token;
		if (tokenStr.isPresent()) {
			token = new VaultToken(tokenStr.get());
		} else {
			File tokenFile = source.getFileFromRequiredParam(CFG_PARAM_TOKEN_FILE.getName());
			token = new VaultToken(tokenFile);
		}

		VaultClient client = new VaultClient(source.getAlias(), token, kvBase, addr, skipVerify);
		if (this.clients.put(client.alias, client) != null) {
			throw new LookupProviderException(this, "Vault client alias already exists: alias=" + client.alias);
		}
	}

	@Override
	public boolean isEnabled() {
		return this.clients != null && !this.clients.isEmpty();
	}

	@Override
	public Optional<String> lookup(String alias, Map<String, Object> args) {
		Optional<String> result = Optional.empty();

		VaultClient client = this.clients.get(alias);
		if (client == null) {
			log.error("Vault client alias not found: provider={}; alias={}", getName(), alias);
			return result;
		}
		String path = getStringArg(args, ARG_KEY.getName());
		String field = getStringArg(args, ARG_FIELD.getName());

		try {
			if (log.isTraceEnabled()) {
				log.trace("vault lookup: path={}, field={}", path, field);
			}

			Optional<String> value = client.getSecret(path, field);
			if (value.isPresent()) {
				log.debug("found lookup key: provider={}; alias={}; source={}; key={}; field={}", getName(),
						client.alias, client.addr, path, field);
			}

			return result;
		} catch (Exception e) {
			log.error(e);
			return result;
		}
	}
}
