package com.axway.yamles.utils.merge.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.merge.files.FilesCommand.FilesArg;
import com.axway.yamles.utils.plugins.Evaluator;
import com.axway.yamles.utils.plugins.ExecutionMode;

public class FileGenerator {
	private final ExecutionMode mode;
	private final List<FilesArg> filesArgs = new ArrayList<>();

	public FileGenerator(ExecutionMode mode) {
		this.mode = Objects.requireNonNull(mode, "configuration mode required");
	}

	public void setFilesArgs(List<FilesArg> args) {
		this.filesArgs.clear();
		if (args != null) {
			this.filesArgs.addAll(args);
		}
	}

	public void apply() throws Exception {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "File Configuration");
		for (FilesArg fa : this.filesArgs) {
			FilesConfig fsc = FilesConfig.loadConfig(fa.files);
			fsc.setConfigSource(fa.files);
			apply(fa.baseDir, fsc);
		}
	}

	private void apply(File baseDir, FilesConfig fsc) throws Exception {
		for (FileConfig fc : fsc.getFileConfigs()) {
			apply(baseDir, fc);
		}
	}

	private void apply(File baseDir, FileConfig fc) throws Exception {
		File targetFile = null;
		String filePath = fc.getPath();

		if (baseDir != null) {
			Path basePath = Paths.get(baseDir.toURI());
			targetFile = basePath.resolve(filePath).toFile();
		} else {
			targetFile = new File(filePath);
		}

		String content = "";

		if (fc.getContent() != null) {
			content = Evaluator.eval(fc.getContent());
		} else if (fc.getTemplate() != null) {
			content = Evaluator.eval(fc.getTemplate());
		} else {
			throw new IllegalStateException("content and template are null");
		}

		if (this.mode == ExecutionMode.CONFIG) {
			Audit.AUDIT_LOG.info("  generate file {} with encoding {} from {}", targetFile.getAbsoluteFile(),
					fc.getEncodingName(), fc.getConfigSource().getAbsolutePath());

			byte[] output;
			if (fc.getEncoding().isPresent()) {
				output = content.getBytes(fc.getEncoding().get());
			} else {
				output = Base64.getDecoder().decode(content);
			}
			
			File targetDir = targetFile.getParentFile();
			if (!targetDir.isDirectory()) {
				if (fc.hasCreateDirs()) {
					targetDir.mkdirs();
				} else {
					throw new IllegalStateException("target directory doesn't exist and auto directory creation disabled: " + targetDir.getAbsolutePath());
				}
			}

			try (OutputStream out = new FileOutputStream(targetFile)) {
				out.write(output);
			}

		} else {
			Audit.AUDIT_LOG.info("  file generation skipped (due to execution mode {}): {}", this.mode,
					targetFile.getAbsolutePath());
		}
	}
}
