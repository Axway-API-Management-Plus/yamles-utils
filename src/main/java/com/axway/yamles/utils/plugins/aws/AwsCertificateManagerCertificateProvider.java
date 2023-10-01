package com.axway.yamles.utils.plugins.aws;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.plugins.AbstractCertificateProvider;
import com.axway.yamles.utils.plugins.CertificateProviderException;
import com.axway.yamles.utils.plugins.CertificateReplacement;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;

import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.AcmClientBuilder;
import software.amazon.awssdk.services.acm.model.GetCertificateRequest;
import software.amazon.awssdk.services.acm.model.GetCertificateResponse;
import software.amazon.awssdk.services.acm.model.ResourceNotFoundException;

public class AwsCertificateManagerCertificateProvider extends AbstractCertificateProvider {

	private static final Logger log = LogManager.getLogger(AwsCertificateManagerCertificateProvider.class);

	public static final ConfigParameter CFG_ARN = new ConfigParameter("arn", true,
			"ARN of the certificate stored in the AWS Certificate Manager", Type.string, false);
	public static final ConfigParameter CFG_CHAIN = new ConfigParameter("chain", false, "Include certificate chain",
			Type.bool, false);

	private AcmClient client;

	public AwsCertificateManagerCertificateProvider() {
		super(CFG_ARN, CFG_CHAIN);
	}

	@Override
	public String getName() {
		return "aws_cm";
	}

	@Override
	public String getSummary() {
		return "Certificates from AWS Certificate Manager";
	}

	@Override
	public String getDescription() {
		return "Retrieves public certificates from AWS Certificate Manager. Currently, private keys are not supported.";
	}

	@Override
	public List<CertificateReplacement> getCertificate(File configSource, String aliasName, Map<String, String> config)
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

		String arn = getConfig(CFG_ARN, config, "");
		boolean addChain = getConfig(CFG_CHAIN, config, "false").equals("true");

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
					Collection<? extends Certificate> certs = cf
							.generateCertificates(new ByteArrayInputStream(chain.getBytes("ASCII")));
					cr.addChain(certs);
				}
			}

			return Arrays.asList(cr);
		} catch (Exception e) {
			throw new CertificateProviderException("error on parsing ACM certificate: " + aliasName, e);
		}
	}
}
