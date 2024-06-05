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
			apply(fa.baseDir, fa.baseDirSrc, fsc);
		}
	}

	private void apply(File baseDir, File baseDirSrc, FilesConfig fsc) throws Exception {
		for (FileConfig fc : fsc.getFileConfigs()) {
			apply(baseDir, baseDirSrc, fc);
		}
	}

	private void apply(File baseDir, File baseDirSrc, FileConfig fc) throws Exception {
		baseDir = resolveBaseDir(fc, baseDir);
		baseDirSrc = resolveBaseDir(fc, baseDirSrc);

		String filePath = fc.getPath();
		File targetFile = resolveFile(baseDir, filePath);

		String content = "";

		if (fc.getContent() != null) {
			content = Evaluator.eval(fc.getContent());
		} else if (fc.getTemplatePath() != null) {
			File template = resolveFile(baseDirSrc, fc.getTemplatePath());
			content = Evaluator.eval(template);
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
					throw new IllegalStateException(
							"target directory doesn't exist and auto directory creation disabled: "
									+ targetDir.getAbsolutePath());
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

	private File resolveBaseDir(FileConfig fc, File baseDir) {
		if (baseDir != null) {
			return baseDir;
		}
		if (fc.getConfigSource() == null) {
			return null;
		}
		return fc.getConfigSource().getParentFile();
	}

	private File resolveFile(File baseDir, String filePath) {
		File resolvedFile = null;
		if (baseDir != null) {
			Path basePath = Paths.get(baseDir.toURI());
			resolvedFile = basePath.resolve(filePath).toFile();
		} else {
			resolvedFile = new File(filePath);
		}
		return resolvedFile;
	}
}
