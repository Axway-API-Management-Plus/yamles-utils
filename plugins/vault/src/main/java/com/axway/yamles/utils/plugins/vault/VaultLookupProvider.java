package com.axway.yamles.utils.plugins.vault;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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
import org.pf4j.Extension;

import com.axway.yamles.utils.plugins.AbstractLookupProvider;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.LookupProviderException;
import com.axway.yamles.utils.plugins.LookupSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Extension
public class VaultLookupProvider extends AbstractLookupProvider {
	private static final String DEFAULT_ADDR = "https://localhost:8200";

	public static final FunctionArgument ARG_KEY = new FunctionArgument("key", true, "Path to KV");
	public static final FunctionArgument ARG_FIELD = new FunctionArgument("field", true, "Field within the KV data");

	public static final ConfigParameter CFG_PARAM_ADDR = new ConfigParameter("addr", false,
			"Address of Vault server [default: " + DEFAULT_ADDR + "]", ConfigParameter.Type.string, false);
	public static final ConfigParameter CFG_PARAM_KV_BASE = new ConfigParameter("kv_base", true,
			"Path of the KV secret engine", ConfigParameter.Type.string, false);
	public static final ConfigParameter CFG_PARAM_SKIP_VERIFY = new ConfigParameter("skip_verify", false,
			"Skip server name verification", ConfigParameter.Type.bool, false);
	public static final ConfigParameter CFG_PARAM_TOKEN = new ConfigParameter("token", false,
			"Token to authorize access to Vault (if token file is not specified).", ConfigParameter.Type.string, true);
	public static final ConfigParameter CFG_PARAM_TOKEN_FILE = new ConfigParameter("token_file", false,
			"Path to token file (if token is not specified).", ConfigParameter.Type.file, false);

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
			this.addr = Objects.requireNonNull(addr).isPresent() ? addr.get() : DEFAULT_ADDR;
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

	protected static class LF extends LookupFunction {
		private static final Logger log = LogManager.getLogger(LF.class);
		private final VaultClient client;

		public LF(String alias, VaultLookupProvider provider, Optional<String> source, VaultClient client) {
			super(alias, provider, source);
			this.client = Objects.requireNonNull(client, "client required");
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			Optional<String> result = Optional.empty();

			String path = getArg(ARG_KEY, args, "");
			String field = getArg(ARG_FIELD, args, "");

			try {
				if (log.isTraceEnabled()) {
					log.trace("vault lookup: path={}, field={}", path, field);
				}

				Optional<String> value = this.client.getSecret(path, field);
				if (value.isPresent()) {
					log.debug("found lookup key: provider={}; alias={}; source={}; key={}; field={}", getName(),
							this.client.alias, this.client.addr, path, field);
				}

				return result;
			} catch (Exception e) {
				log.error(e);
				return result;
			}
		}
	}

	public VaultLookupProvider() {
		super();
		add(ARG_KEY, ARG_FIELD);
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
	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException {
		// get configuration parameters
		String kvBase = source.getConfig(CFG_PARAM_KV_BASE, "");
		Optional<String> tokenStr = Optional.ofNullable(source.getConfig(CFG_PARAM_TOKEN, null));
		Optional<String> addr = Optional.ofNullable(source.getConfig(CFG_PARAM_ADDR, null));
		boolean skipVerify = source.getConfig(CFG_PARAM_SKIP_VERIFY, "false").equals("true");

		VaultToken token;
		if (tokenStr.isPresent()) {
			token = new VaultToken(tokenStr.get());
		} else {
			Optional<File> tokenFile = source.getFileFromConfig(CFG_PARAM_TOKEN_FILE);
			if (!tokenFile.isPresent()) {
				throw new LookupProviderException(this, "token file missing: alias=" + source.getAlias());
			}
			token = new VaultToken(tokenFile.get());
		}

		Optional<LookupFunction> clf = checkOnlyLookupFunction(source);
		if (clf.isPresent())
			return clf.get();

		VaultClient client = new VaultClient(source.getAlias(), token, kvBase, addr, skipVerify);

		return new LF(source.getAlias(), this, source.getConfigSource(), client);
	}
}
