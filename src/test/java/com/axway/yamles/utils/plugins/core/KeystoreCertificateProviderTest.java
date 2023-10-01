package com.axway.yamles.utils.plugins.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.merge.LookupManager;
import com.axway.yamles.utils.plugins.CertificateReplacement;

public class KeystoreCertificateProviderTest {
	@BeforeAll
	static void initLookupManager() {
		LookupManager.getInstance();
	}

	@Test
	void testKeystoreFromData() throws Exception {

		InputStream in = KeystoreCertificateProviderTest.class.getResourceAsStream("/keystore.p12.b64");

		StringBuilder keystoreB64 = new StringBuilder();
		try (Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			int c = 0;
			while ((c = reader.read()) != -1) {
				keystoreB64.append((char) c);
			}
		}

		Map<String, String> config = new HashMap<>();
		config.put("type", "PKCS12");
		config.put("pass", "changeme");
		config.put("data", keystoreB64.toString());

		KeystoreCertificateProvider cp = new KeystoreCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificate(new File("cert-config.yaml"), "server", config);

		assertEquals(1, crs.size());
		assertEquals("server", crs.get(0).getAlias());
		assertTrue(crs.get(0).getCert().isPresent());
		assertTrue(crs.get(0).getKey().isPresent());
	}

	@Test
	void testKeystoreFromPath() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		File file = new File(classLoader.getResource("keystore.p12").getFile());

		Map<String, String> config = new HashMap<>();
		config.put("type", "PKCS12");
		config.put("pass", "changeme");
		config.put("path", file.getAbsolutePath());

		KeystoreCertificateProvider cp = new KeystoreCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificate(new File("cert-config.yaml"), "server", config);

		assertEquals(1, crs.size());
		assertEquals("server", crs.get(0).getAlias());
		assertTrue(crs.get(0).getCert().isPresent());
		assertTrue(crs.get(0).getKey().isPresent());
	}
}
