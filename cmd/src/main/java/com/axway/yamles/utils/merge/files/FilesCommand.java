package com.axway.yamles.utils.merge.files;

import java.io.File;
import java.util.List;

import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;
import com.axway.yamles.utils.merge.MergeCommand;
import com.axway.yamles.utils.merge.ProviderManager;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "files", description = "Generate files.", mixinStandardHelpOptions = true)
public class FilesCommand extends AbstractLookupEnabledCommand {

	public static class FilesArg {
		public FilesArg() {
		}

		public FilesArg(File baseDir, File files) {
			this.baseDir = baseDir;
			this.files = files;
		}

		@Option(names = {
				"--files-base-dir" }, description = "Base directory for generated files.", paramLabel = "DIR", required = false)
		public File baseDir;

		@Option(names = {
				"--files" }, description = "Files generator configuration.", paramLabel = "FILE", required = true)
		public File files;
	}
	
	@ArgGroup(exclusive = false, multiplicity = "1..*")
	private List<FilesArg> args;
	
	@ParentCommand
	private MergeCommand parentCommand;


	public FilesCommand() {
		super();
	}

	@Override
	public Integer call() throws Exception {
		initializeProviderManager(this.parentCommand.getMode());

		ExecutionMode mode = ProviderManager.getInstance().getConfigMode();

		FileGenerator fg = new FileGenerator(mode);
		fg.setFilesArgs(args);
		fg.apply();

		return 0;
	}
}
