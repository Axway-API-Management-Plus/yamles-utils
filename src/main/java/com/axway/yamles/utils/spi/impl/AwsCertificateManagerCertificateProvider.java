package com.axway.yamles.utils.spi.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.CertificateProviderException;
import com.axway.yamles.utils.spi.CertificateReplacement;

import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.AcmClientBuilder;
import software.amazon.awssdk.services.acm.model.GetCertificateRequest;
import software.amazon.awssdk.services.acm.model.GetCertificateResponse;
import software.amazon.awssdk.services.acm.model.ResourceNotFoundException;

public class AwsCertificateManagerCertificateProvider extends AbstractCertificateProvider {

	private static final Logger log = LogManager.getLogger(AwsCertificateManagerCertificateProvider.class);
	
	public static final String CFG_ARN = "arn";
	public static final String CFG_CHAIN = "chain";

	private AcmClient client;

	@Override
	public String getName() {
		return "aws_cm";
	}

	@Override
	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {

		synchronized (this) {
			try {
				if (this.client == null) {
					AcmClientBuilder builder = AcmClient.builder();
					this.client = builder.build();
					log.debug("certificate provider for AWS Certificate Manager initialized");
				}
			} catch (Exception e) {
				throw new CertificateProviderException("error on initializing AWS Certificate Manager", e);
			}
		}

		String arn = getRequiredConfig(config, CFG_ARN);
		boolean addChain = getConfig(config, CFG_CHAIN, "false").equals("true");

		GetCertificateResponse result = null;
		try {
			GetCertificateRequest req = GetCertificateRequest.builder().certificateArn(arn).build();
			result = client.getCertificate(req);
		} catch (ResourceNotFoundException e) {
			throw new CertificateProviderException("certificate not found in ACM: " + aliasName);
		} catch (Exception e) {
			throw new CertificateProviderException("error on getting certificate from ACM: " + aliasName, e);
		}

		try {
			String cert = result.certificate();
			
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			Certificate c = cf.generateCertificate(new ByteArrayInputStream(cert.getBytes("ASCII")));
			CertificateReplacement cr = new CertificateReplacement(aliasName, c);
			
			if (addChain) {
				String chain = result.certificateChain();
				if (chain != null) {
					Collection<? extends Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(chain.getBytes("ASCII")));
					cr.addChain(certs);
				}
			}
			
			return cr;
		} catch (Exception e) {
			throw new CertificateProviderException("error on parsing ACM certificate: " + aliasName, e);
		}
	}
}
