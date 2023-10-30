package com.axway.yamles.utils.merge.certs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CertificatesConfigTest {

	@Test
	void test() {
		String yaml = "---\n" //
				+ "certificates:\n" //
				+ "  alias:\n" //
				+ "    provider: file\n" //
				+ "    config:\n" //
				+ "      cert: cert.pem\n" //
				+ "      key: key.pem\n" //
				+ "      pass: changeme\n" //
				+ "...";

		CertificatesConfig cc = CertificatesConfig.loadConfig(yaml);
		assertNotNull(cc.getAliases());
		assertNotNull(cc.getAliases().get("alias"));

		Alias a = cc.getAliases().get("alias");
		assertEquals("alias", a.getName());
		assertEquals("string", a.getConfigSource().getName());
		assertEquals("file", a.getProvider());
		assertNotNull(a.getConfig());

		assertEquals("cert.pem", a.getConfig().get("cert"));
		assertEquals("key.pem", a.getConfig().get("key"));
		assertEquals("changeme", a.getConfig().get("pass"));
	}

	@Test
	void empty_certificate_config() {
		String yaml = "---\n" //
				+ "certificates:";

		CertificatesConfig cc = CertificatesConfig.loadConfig(yaml);
		assertNotNull(cc.getAliases());
		assertEquals(0, cc.getAliases().size());
	}
}
