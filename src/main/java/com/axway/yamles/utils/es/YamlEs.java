package com.axway.yamles.utils.es;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.ValueNodeSet;
import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class YamlEs {

	private static final Logger log = LogManager.getLogger(YamlEs.class);

	public static final String ENV_CONF = "Environment Configuration";
	public static final String ENV_CONF_CERT = "Certificate Store";

	private final File projectDir;
	private final ValueNodeSet requiredValues;

	public YamlEs(File projectDir) {
		if (!isValid(projectDir))
			throw new IllegalArgumentException("invalid project dir: " + projectDir);
		log.info("project: {}", projectDir.getAbsoluteFile());

		this.projectDir = projectDir;

		if (getValuesFile().exists()) {
			requiredValues = new ValueNodeSet(Yaml.load(getValuesFile()));
		} else {
			requiredValues = null;
		}
	}

	public Optional<ValueNodeSet> getRequiredValues() {
		return Optional.ofNullable(this.requiredValues);
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

	public void writeCertificate(String alias, Certificate cert, Optional<Key> key)
			throws IOException, CertificateEncodingException {
		CertFiles files = new CertFiles(this, alias);
		files.write(cert, key);
	}

	public static Optional<String> getEnityType(ObjectNode yaml) {
		JsonNode type = yaml.get("type");
		if (type == null || !type.isTextual())
			return Optional.empty();
		return Optional.of(type.asText());
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
