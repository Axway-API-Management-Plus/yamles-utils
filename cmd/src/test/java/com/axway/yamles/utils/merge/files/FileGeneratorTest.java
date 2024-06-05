package com.axway.yamles.utils.merge.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.axway.yamles.utils.merge.files.FilesCommand.FilesArg;
import com.axway.yamles.utils.plugins.Evaluator;
import com.axway.yamles.utils.plugins.ExecutionMode;

public class FileGeneratorTest {

	@TempDir
	private File tempDir;

	@BeforeAll
	static void setupTemplateEngine() {
		Evaluator.setDefaultTemplateEngine();
	}

	@Test
	void generateFiles() throws Exception {
		String testTxtName = "test.txt";

		File testTxt = new File(tempDir, testTxtName);
		File testBin = new File(tempDir, "test.bin");
		File testTpl = new File(tempDir, "test.tpl");
		File testNewDirFile = new File(tempDir, "newdir/test.txt");

		File template = new File(tempDir, "template.tpl");
		FileUtils.write(template, "", Charset.forName("UTF-8"));

		// generate file configuration
		String text = "Hello World";
		byte[] bytes = "Hello World".getBytes(Charset.defaultCharset());
		String b64HelloWorld = Base64.getEncoder().encodeToString(bytes);

		File fileConfig = new File(tempDir, "file-config.yaml");

		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - path: " + testTxtName + "\n" //
				+ "    encoding: UTF-8\n" //
				+ "    content: " + text + "\n" //
				+ "  - path: " + testBin.getAbsolutePath() + "\n" //
				+ "    encoding: binary\n" //
				+ "    content: " + b64HelloWorld + "\n" //
				+ "  - path: " + testTpl.getAbsolutePath() + "\n" //
				+ "    encoding: UTF-8\n" //
				+ "    template: " + template.getAbsolutePath() + "\n" //
				+ "  - path: " + testNewDirFile.getAbsolutePath() + "\n" //
				+ "    encoding: UTF-8\n" //
				+ "    content: " + text + "\n" //
				+ "    createDirs: true\n" //
				+ "...";

		try (FileWriter out = new FileWriter(fileConfig)) {
			out.write(yaml);
		}

		List<FilesArg> fa = new ArrayList<>();
		fa.add(new FilesArg(tempDir, null, fileConfig));

		// generate files
		FileGenerator fg = new FileGenerator(ExecutionMode.CONFIG);
		fg.setFilesArgs(fa);
		fg.apply();

		// test generation
		try (BufferedReader reader = new BufferedReader(new FileReader(testTxt))) {
			String writtenText = reader.readLine();
			assertEquals(text, writtenText);
		}
		try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(testBin))) {
			byte[] writtenBytes = new byte[bytes.length];
			input.read(writtenBytes);

			assertTrue(Arrays.equals(bytes, writtenBytes));
			assertEquals(-1, input.read(writtenBytes));
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(testTpl))) {
			String writtenText = reader.readLine();
			assertEquals(template.getAbsolutePath(), writtenText);
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(testNewDirFile))) {
			String writtenText = reader.readLine();
			assertEquals(text, writtenText);
		}
	}

	@Test
	void failOnNonExistingDirectory() throws Exception {
		File testNoDirFile = new File(tempDir, "nodir/test.txt");

		// generate file configuration
		String text = "Hello World";

		File fileConfig = new File(tempDir, "file-config.yaml");

		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - path: " + testNoDirFile.getAbsolutePath() + "\n" //
				+ "    encoding: UTF-8\n" //
				+ "    content: " + text + "\n" //
				+ "...";

		try (FileWriter out = new FileWriter(fileConfig)) {
			out.write(yaml);
		}

		List<FilesArg> fa = new ArrayList<>();
		fa.add(new FilesArg(tempDir, null, fileConfig));

		// generate files
		FileGenerator fg = new FileGenerator(ExecutionMode.CONFIG);
		fg.setFilesArgs(fa);

		assertThrows(IllegalStateException.class, () -> fg.apply());
	}

	@Test
	void useBaseDirs() throws Exception {
		File templatesDir = new File(tempDir, "templates");
		templatesDir.mkdirs();

		File fileConfig = new File(tempDir, "file-config.yaml");

		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - path: generated.txt\n" //
				+ "    encoding: UTF-8\n" //
				+ "    template: generated.tpl\n" //
				+ "...";

		try (FileWriter out = new FileWriter(fileConfig)) {
			out.write(yaml);
		}

		try (FileWriter out = new FileWriter(new File(templatesDir, "generated.tpl"))) {
			out.write("Template");
		}

		List<FilesArg> fa = new ArrayList<>();
		fa.add(new FilesArg(tempDir, templatesDir, fileConfig));

		// generate file
		FileGenerator fg = new FileGenerator(ExecutionMode.CONFIG);
		fg.setFilesArgs(fa);
		fg.apply();

		try (BufferedReader reader = new BufferedReader(new FileReader(new File(tempDir, "generated.txt")))) {
			String writtenText = reader.readLine();
			assertEquals(new File(templatesDir, "generated.tpl").getAbsolutePath(), writtenText);
		}
	}
}
