package com.axway.yamles.utils.plugins.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.merge.LookupManager;
import com.axway.yamles.utils.plugins.CertificateReplacement;

public class FileCertificateProviderTest {

	@BeforeAll
	static void initLookupManager() {
		LookupManager.getInstance();
	}

	@Test
	void certifciateFromFileDER() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		File file = new File(classLoader.getResource("cert-ca.crt").getFile());

		Map<String, String> config = new HashMap<>();
		config.put("path", file.getAbsolutePath());

		FileCertificateProvider cp = new FileCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificates(new File("cert-config.yaml"), "root-ca", config);

		assertEquals(1, crs.size());
		assertTrue(crs.get(0).getCert().isPresent());
		assertTrue(crs.get(0).getCert().get() instanceof X509Certificate);
		assertEquals("CN=root-ca, O=Axway, L=Berlin, C=DE",
				((X509Certificate) crs.get(0).getCert().get()).getSubjectDN().toString());
	}

	@Test
	void certifciateFromFilePEM() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		File file = new File(classLoader.getResource("cert-ca.pem").getFile());

		Map<String, String> config = new HashMap<>();
		config.put("path", file.getAbsolutePath());

		FileCertificateProvider cp = new FileCertificateProvider();
		List<CertificateReplacement> crs = cp.getCertificates(new File("cert-config.yaml"), "root-ca", config);

		assertEquals(1, crs.size());
		assertTrue(crs.get(0).getCert().isPresent());
		assertTrue(crs.get(0).getCert().get() instanceof X509Certificate);
		assertEquals("CN=root-ca, O=Axway, L=Berlin, C=DE",
				((X509Certificate) crs.get(0).getCert().get()).getSubjectDN().toString());
	}
}
