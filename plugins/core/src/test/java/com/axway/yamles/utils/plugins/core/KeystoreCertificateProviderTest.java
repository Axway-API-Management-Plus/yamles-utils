package com.axway.yamles.utils.plugins.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.plugins.CertificateReplacement;

public class KeystoreCertificateProviderTest {

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
		config.put(KeystoreCertificateProvider.CFG_TYPE.getName(), "PKCS12");
		config.put(KeystoreCertificateProvider.CFG_PASSPHRASE.getName(), "changeme");
		config.put(KeystoreCertificateProvider.CFG_DATA.getName(), keystoreB64.toString());

		KeystoreCertificateProvider cp = new KeystoreCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificates(new File("cert-config.yaml"), "server", config);

		assertEquals(1, crs.size());
		assertEquals("server", crs.get(0).getAlias().get());
		assertTrue(crs.get(0).getCert().isPresent());
		assertTrue(crs.get(0).getKey().isPresent());
	}

	@Test
	void testKeystoreFromPathNoAlias() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		File file = new File(classLoader.getResource("keystore.p12").getFile());

		Map<String, String> config = new HashMap<>();
		config.put(KeystoreCertificateProvider.CFG_TYPE.getName(), "PKCS12");
		config.put(KeystoreCertificateProvider.CFG_PASSPHRASE.getName(), "changeme");
		config.put(KeystoreCertificateProvider.CFG_PATH.getName(), file.getAbsolutePath());

		KeystoreCertificateProvider cp = new KeystoreCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificates(new File("cert-config.yaml"), "server", config);

		assertEquals(1, crs.size());
		assertEquals("server", crs.get(0).getAlias().get());
		assertTrue(crs.get(0).getCert().isPresent());
		assertTrue(crs.get(0).getKey().isPresent());
	}
	
	@Test
	void testKeystoreFromPathWithAliasAndChain() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		File file = new File(classLoader.getResource("keystore.p12").getFile());

		Map<String, String> config = new HashMap<>();
		config.put(KeystoreCertificateProvider.CFG_TYPE.getName(), "PKCS12");
		config.put(KeystoreCertificateProvider.CFG_PASSPHRASE.getName(), "changeme");
		config.put(KeystoreCertificateProvider.CFG_PATH.getName(), file.getAbsolutePath());
		config.put(KeystoreCertificateProvider.CFG_ALIAS.getName(), "server");
		config.put(KeystoreCertificateProvider.CFG_CHAIN.getName(), "true");

		KeystoreCertificateProvider cp = new KeystoreCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificates(new File("cert-config.yaml"), "server", config);

		assertEquals(1, crs.size());
		CertificateReplacement cr = crs.get(0);
		assertEquals("server", cr.getAlias().get());
		assertTrue(cr.getCert().isPresent());
		assertTrue(cr.getKey().isPresent());

		// check chain
		assertEquals(2, cr.getChain().size());
		X509Certificate cert = (X509Certificate) cr.getCert().get();
		X509Certificate subCA = (X509Certificate) cr.getChain().get(0);
		X509Certificate rootCA = (X509Certificate) cr.getChain().get(1);
		
		assertEquals(cert.getIssuerX500Principal(), subCA.getSubjectX500Principal());
		assertEquals(subCA.getIssuerX500Principal(), rootCA.getSubjectX500Principal());
	}
	
	@Test
	void testKeystoreAllCerts() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		File file = new File(classLoader.getResource("truststore.p12").getFile());

		Map<String, String> config = new HashMap<>();
		config.put(KeystoreCertificateProvider.CFG_TYPE.getName(), "PKCS12");
		config.put(KeystoreCertificateProvider.CFG_PASSPHRASE.getName(), "changeme");
		config.put(KeystoreCertificateProvider.CFG_PATH.getName(), file.getAbsolutePath());
		config.put(KeystoreCertificateProvider.CFG_ALIAS.getName(), ".*");
		config.put(KeystoreCertificateProvider.CFG_NOKEY.getName(), "true");

		KeystoreCertificateProvider cp = new KeystoreCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificates(new File("cert-config.yaml"), "trust", config);

		assertEquals(3, crs.size());
	}
	
	@Test
	void testKeystoreFilterCerts() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		File file = new File(classLoader.getResource("truststore.p12").getFile());

		Map<String, String> config = new HashMap<>();
		config.put(KeystoreCertificateProvider.CFG_TYPE.getName(), "PKCS12");
		config.put(KeystoreCertificateProvider.CFG_PASSPHRASE.getName(), "changeme");
		config.put(KeystoreCertificateProvider.CFG_PATH.getName(), file.getAbsolutePath());
		config.put(KeystoreCertificateProvider.CFG_ALIAS.getName(), "(trust|sub)-ca");
		config.put(KeystoreCertificateProvider.CFG_NOKEY.getName(), "true");

		KeystoreCertificateProvider cp = new KeystoreCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificates(new File("cert-config.yaml"), "trust", config);

		assertEquals(2, crs.size());
	}
}
