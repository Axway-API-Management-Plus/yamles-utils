package com.axway.yamles.utils.helper;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class YamlEs {

	public static final class CertFiles {
		private final String alias;
		private final File yamlFile;
		private final File certFile;
		private final File keyFile;

		public CertFiles(YamlEs project, String alias) {
			this.alias = alias;
			File certStore = project.getCertStoreDir();
			this.yamlFile = new File(certStore, alias + ".yaml");
			this.certFile = new File(certStore, alias + "-cert.pem");
			this.keyFile = new File(certStore, alias + "-key.pem");
		}

		public void write(Certificate cert, Optional<Key> key) throws IOException, CertificateEncodingException {
			Objects.requireNonNull(cert);
			
			removeFiles();

			ObjectNode yaml = buildYaml(key);
			Yaml.write(this.yamlFile, yaml);

			PemFile.write(this.certFile, cert.getEncoded());
			if (key.isPresent()) {
				PemFile.write(this.keyFile, key.get().getEncoded());
			}
		}
		
		public void removeFiles() {
			this.yamlFile.delete();
			this.certFile.delete();
			this.keyFile.delete();
		}

		private ObjectNode buildYaml(Optional<Key> key) {
			ObjectNode yaml = Yaml.createObjectNode();

			yaml.put("type", "Certificate");
			ObjectNode fields = yaml.putObject("fields");
			fields.put("dname", this.alias);
			fields.put("issuer", "/null");
			fields.put("engine", "RAW");
			fields.put("certificateRealm", "");
			fields.put("content", "{{file '" + this.certFile.getName() + "'}}");
			if (key.isPresent()) {
				fields.put("key", "{{file '" + this.keyFile.getName() + "'}}");
			}

			return yaml;
		}
	}

	private static final Logger log = LogManager.getLogger(YamlEs.class);

	public static final String ENV_CONF = "Environment Configuration";
	public static final String ENV_CONF_CERT = "Certificate Store";

	private final File projectDir;

	public YamlEs(File projectDir) {
		if (!isValid(projectDir))
			throw new IllegalArgumentException("invalid project dir: " + projectDir);
		log.info("project: {}", projectDir.getAbsoluteFile());
		this.projectDir = projectDir;
	}
	
	public File getProjectDir() {
		return this.projectDir;
	}
	
	public String getAbsolutePath() {
		return this.projectDir.getAbsolutePath();
	}

	public File getValuesFile() {
		return new File(this.projectDir, "values.yaml");
	}

	public File getCertStoreDir() {
		return getCertStoreDir(this.projectDir);
	}

	public void writeValues(ObjectNode values) throws IOException {
		Yaml.write(getValuesFile(), values);
	}

	public void removeCertificate(String alias) {
		CertFiles files = new CertFiles(this, alias);
		files.removeFiles();
	}

	public void writeCertificate(String alias, Certificate cert, Optional<Key> key) throws IOException, CertificateEncodingException {
		CertFiles files = new CertFiles(this, alias);
		files.write(cert, key);
	}

	public static boolean isValid(File projectDir) {
		boolean valid = true;
		log.debug("check project directory: {}", projectDir);
		if (projectDir == null || !projectDir.isDirectory()) {
			log.error("not a directory: {}", projectDir);
			valid = false;
		}
		File parent = new File(projectDir, "_parent.yaml");
		if (!parent.exists()) {
			log.error("missing parent file: {}", parent);
			valid = false;
		}
		File envConfDir = getEnvironmentConfigDir(projectDir);
		if (!envConfDir.isDirectory()) {
			log.error("missing environment config: {}", envConfDir);
			valid = false;
		}
		File certDir = getCertStoreDir(projectDir);
		if (!certDir.isDirectory()) {
			log.error("missing certificate store: {}", envConfDir);
			valid = false;
		}
		return valid;
	}

	private static File getEnvironmentConfigDir(File projectDir) {
		return new File(projectDir, ENV_CONF);
	}

	private static File getCertStoreDir(File projectDir) {
		return new File(getEnvironmentConfigDir(projectDir), ENV_CONF_CERT);
	}
}
