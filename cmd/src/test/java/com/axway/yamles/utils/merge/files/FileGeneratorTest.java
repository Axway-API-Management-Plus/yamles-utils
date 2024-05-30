package com.axway.yamles.utils.merge.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.axway.yamles.utils.merge.files.FilesCommand.FilesArg;
import com.axway.yamles.utils.plugins.Evaluator;
import com.axway.yamles.utils.plugins.ExecutionMode;

public class FileGeneratorTest {
	
	@TempDir
	private File tempDir;
	
	@Test
	void generateFiles() throws Exception {
		String testTxtName = "test.txt";

		File testTxt = new File(tempDir, testTxtName);
		File testBin = new File(tempDir, "test.bin");
		File testTpl = new File(tempDir, "test.tpl");
		
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
				+ "    content: " + text+ "\n" //
				+ "  - path: " + testBin.getAbsolutePath() + "\n" //
				+ "    encoding: binary\n" //
				+ "    content: " + b64HelloWorld + "\n" //
				+ "  - path: " + testTpl.getAbsolutePath() + "\n" //
				+ "    encoding: UTF-8\n" //
				+ "    template: " + template.getAbsolutePath() + "\n" //
				+ "...";
		
		try (FileWriter out = new FileWriter(fileConfig)) {
			out.write(yaml);
		}
		
		List<FilesArg> fa = new ArrayList<>();
		fa.add(new FilesArg(tempDir, fileConfig));
		

		// generate files
		Evaluator.setDefaultTemplateEngine();

		FileGenerator fg = new FileGenerator(ExecutionMode.CONFIG);
		fg.setFilesArgs(fa);
		fg.apply();

		// test generation
		try(BufferedReader reader = new BufferedReader(new FileReader(testTxt))) {
			String writtenText = reader.readLine();
			assertEquals(text,  writtenText);
		}
		try(BufferedInputStream input = new BufferedInputStream(new FileInputStream(testBin))) {
			byte[] writtenBytes = new byte[bytes.length];
			input.read(writtenBytes);
			
			assertTrue(Arrays.equals(bytes, writtenBytes));
			assertEquals(-1, input.read(writtenBytes));
		}
		try(BufferedReader reader = new BufferedReader(new FileReader(testTpl))) {
			String writtenText = reader.readLine();
			assertEquals(template.getAbsolutePath(),  writtenText);
		}
	}
}
