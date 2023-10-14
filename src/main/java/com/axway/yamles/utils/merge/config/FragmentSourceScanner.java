package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class FragmentSourceScanner {
	private static final Logger log = LogManager.getLogger(FragmentSourceScanner.class);

	private static final FilenameFilter YAML_FILE_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			assert name != null;
			name = name.toLowerCase();
			return name.endsWith(".yml") || name.endsWith(".yaml");
		}
	};

	private List<File> directories = new ArrayList<>();
	private List<FragmentSource> sources = new ArrayList<>();

	public FragmentSourceScanner() {

	}

	public void addDirectory(File dir) {
		if (dir == null) {
			return;
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("not a directory: " + dir.getAbsolutePath());
		}
		this.directories.add(dir);
		log.debug("directory '{}' add to scanner", dir.getPath());
	}

	public void addDirectories(List<File> dirs) {
		if (dirs == null) {
			return;
		}
		dirs.forEach((d) -> addDirectory(d));
	}

	public void scan() {
		this.sources.clear();

		for (File dir : this.directories) {
			scan(dir);
		}
	}

	public List<FragmentSource> getSources() {
		return this.sources;
	}

	private void scan(File dir) {
		log.debug("scan directory '{}' for fragment files", dir.getAbsoluteFile());

		String[] fileNames = dir.list(YAML_FILE_FILTER);
		Arrays.sort(fileNames);

		for (String fileName : fileNames) {
			File file = new File(dir, fileName);
			this.sources.add(FragmentSourceFactory.load(file));
			log.debug("load fragment: {}", file.getAbsoluteFile());
		}
	}
}
