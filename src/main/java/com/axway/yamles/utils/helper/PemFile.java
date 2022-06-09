package com.axway.yamles.utils.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Objects;

public class PemFile {
	public static final String LINE_SEPARATOR = "\n";

	private PemFile() {
	}

	public static void write(File file, byte[] data) throws IOException {
		Objects.requireNonNull(file);
		Base64.Encoder encoder = Base64.getMimeEncoder(76, LINE_SEPARATOR.getBytes());
		Files.write(file.toPath(), encoder.encode(data));
	}

}
