package com.axway.yamles.utils.es;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CertFiles {
	public static final String LINE_SEPARATOR = "\n";

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

		write(this.certFile, cert.getEncoded());
		if (key.isPresent()) {
			write(this.keyFile, key.get().getEncoded());
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

	private void write(File file, byte[] data) throws IOException {
		Objects.requireNonNull(file);
		Base64.Encoder encoder = Base64.getMimeEncoder(76, LINE_SEPARATOR.getBytes());
		Files.write(file.toPath(), encoder.encode(data));
	}
}
