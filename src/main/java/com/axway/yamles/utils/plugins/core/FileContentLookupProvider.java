package com.axway.yamles.utils.plugins.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.pf4j.Extension;

import com.axway.yamles.utils.plugins.AbstractLookupProvider;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.LookupProviderException;
import com.axway.yamles.utils.plugins.LookupSource;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;

@Extension
public class FileContentLookupProvider extends AbstractLookupProvider {
	private static final String ENCODE_DEFAULT = "UTF-8";
	private static final String ENCODE_BIN = "binary";

	protected static FunctionArgument ARG_KEY = new FunctionArgument("key", true, "Path to file.");
	protected static FunctionArgument ARG_ENCODING = new FunctionArgument("encoding", false,
			"Encoding of the file content. Use '" + ENCODE_BIN + "' for binary files. If missing '" + ENCODE_DEFAULT
					+ "' is assumed.");

	protected static final ConfigParameter CFG_PARAM_BASE_DIR = new ConfigParameter("base", false,
			"Base directory for relative files. If missing, files are relative to the lookup configuration file.",
			Type.file, false);

	static class LF extends LookupFunction {
		private final File baseDir;

		public LF(String alias, FileContentLookupProvider provider, Optional<String> source, File baseDir) {
			super(alias, provider, source);
			this.baseDir = baseDir;
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			Optional<String> result = Optional.empty();

			String encoding = getArg(ARG_ENCODING, args, ENCODE_DEFAULT);
			String filePath = getArg(ARG_KEY, args, "");
			if (filePath == null || filePath.isEmpty()) {
				throw new LookupFunctionException(this, "file not specified");
			}

			File file;
			if (this.baseDir != null) {
				Path basePath = Paths.get(this.baseDir.toURI());
				file = basePath.resolve(filePath).toFile();
			} else {
				file = new File(filePath);
			}

			try {
				if (ENCODE_BIN.equals(encoding)) {
					byte[] contentBin = FileUtils.readFileToByteArray(file);
					String content = Base64.getEncoder().encodeToString(contentBin);
					result = Optional.of(content);
				} else {
					String content = FileUtils.readFileToString(file, encoding);
					result = Optional.of(content);
				}
			} catch (IOException e) {
				throw new LookupFunctionException(this, "error on reading file", e);
			}

			return result;
		}
	}

	public FileContentLookupProvider() {
		super();
		add(ARG_KEY, ARG_ENCODING);
		add(CFG_PARAM_BASE_DIR);
	}

	@Override
	public String getName() {
		return "file";
	}

	@Override
	public String getSummary() {
		return "Lookup file content.";
	}

	@Override
	public String getDescription() {
		return "Reads the content of a file.";
	}

	@Override
	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException {
		File baseDir = source.getBaseDirFromConfig(CFG_PARAM_BASE_DIR).get();
		
		Optional<LookupFunction> clf = checkOnlyLookupFunction(source);
		if (clf.isPresent())
			return clf.get();

		return new LF(source.getAlias(), this, source.getConfigSource(), baseDir);
	}
}
