package com.axway.yamles.utils.spi.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.spi.CertificateReplacement;

public class FileCertificateProviderTest {

	@Test
	void certifciateFromFileDER() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("cert-ca.crt").getFile());
        
		Map<String, String> config = new HashMap<>();
		config.put("path", file.getAbsolutePath());

		FileCertificateProvider cp = new FileCertificateProvider();
		CertificateReplacement cr = cp.getCertificate(new File("cert-config.yaml"), "root-ca", config);
		
		assertEquals("root-ca", cr.getAlias());
		assertTrue(cr.getCert().isPresent());
		assertTrue(cr.getCert().get() instanceof X509Certificate);
		assertEquals("CN=root-ca, O=Axway, L=Berlin, C=DE", ((X509Certificate)cr.getCert().get()).getSubjectDN().toString());
	}
	
	@Test
	void certifciateFromFilePEM() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("cert-ca.pem").getFile());
        
		Map<String, String> config = new HashMap<>();
		config.put("path", file.getAbsolutePath());

		FileCertificateProvider cp = new FileCertificateProvider();
		CertificateReplacement cr = cp.getCertificate(new File("cert-config.yaml"), "root-ca", config);
		
		assertEquals("root-ca", cr.getAlias());
		assertTrue(cr.getCert().isPresent());
		assertTrue(cr.getCert().get() instanceof X509Certificate);
		assertEquals("CN=root-ca, O=Axway, L=Berlin, C=DE", ((X509Certificate)cr.getCert().get()).getSubjectDN().toString());
	}
}