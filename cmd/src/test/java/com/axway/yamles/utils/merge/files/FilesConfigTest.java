package com.axway.yamles.utils.merge.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;

import org.junit.jupiter.api.Test;

public class FilesConfigTest {

	@Test
	void readValidConfig() throws Exception {
		String b64HelloWorld = Base64.getEncoder().encodeToString("Hello World".getBytes(Charset.defaultCharset()));

		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - path: test1.txt\n" //
				+ "    encoding: UTF-8\n" //
				+ "    content: Hello World1\n" //
				+ "  - path: test2.txt\n" //
				+ "    encoding: ISO-8859-1\n" //
				+ "    content: Hello World2\n" //
				+ "    createDirs: true\n" //
				+ "  - path: test.bin\n" //
				+ "    encoding: binary\n" //
				+ "    content: " + b64HelloWorld + "\n" //
				+ "  - path: empty.txt\n" //
				+ "    encoding: UTF-8\n" //
				+ "    content: \"\" \n" //
				+ "  - path: empty.txt\n" //
				+ "    encoding: UTF-8\n" //
				+ "    template: /template.tpl\n" //
				+ "...";

		FilesConfig fsc = FilesConfig.loadConfig(yaml);
		assertEquals(5, fsc.getFileConfigs().size());
		
		FileConfig fc;
		
		fc = fsc.getFileConfigs().get(0);
		assertEquals("test1.txt", fc.getPath());
		assertEquals("UTF-8", fc.getEncoding().get().name());
		assertEquals("Hello World1", fc.getContent());
		assertNull(fc.getTemplate());
		assertFalse(fc.hasCreateDirs());

		fc = fsc.getFileConfigs().get(1);
		assertEquals("test2.txt", fc.getPath());
		assertEquals("ISO-8859-1", fc.getEncoding().get().name());
		assertEquals("Hello World2", fc.getContent());
		assertNull(fc.getTemplate());
		assertTrue(fc.hasCreateDirs());

		fc = fsc.getFileConfigs().get(2);
		assertEquals("test.bin", fc.getPath());
		assertFalse(fc.getEncoding().isPresent());
		assertEquals(FileConfig.ENCODING_BINARY, fc.getEncodingName());
		assertEquals(b64HelloWorld, fc.getContent());
		assertNull(fc.getTemplate());
		
		fc = fsc.getFileConfigs().get(3);
		assertEquals("empty.txt", fc.getPath());
		assertEquals("UTF-8", fc.getEncoding().get().name());
		assertTrue(fc.getContent().isEmpty());
		assertNull(fc.getTemplate());

		fc = fsc.getFileConfigs().get(4);
		assertEquals("empty.txt", fc.getPath());
		assertEquals("UTF-8", fc.getEncoding().get().name());
		assertNull(fc.getContent());
		assertNotNull(fc.getTemplate());
		assertEquals(File.separator + "template.tpl", fc.getTemplate().getPath());
	}

	@Test
	void readInvalidConfig_Missing_path() throws Exception {
		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - encoding: UTF-8\n" //
				+ "    content: Hello World1\n" //
				+ "...";

		assertThrows(FilesConfigException.class, () -> FilesConfig.loadConfig(yaml));
	}

	@Test
	void readInvalidConfig_Missing_encoding() throws Exception {
		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - path: test1.txt\n" //
				+ "    content: Hello World1\n" //
				+ "...";

		assertThrows(FilesConfigException.class, () -> FilesConfig.loadConfig(yaml));
	}
	
	@Test
	void readInvalidConfig_Missing_content() throws Exception {
		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - path: test1.txt\n" //
				+ "    encoding: UTF-8\n" //
				+ "...";

		assertThrows(FilesConfigException.class, () -> FilesConfig.loadConfig(yaml));
	}
	
	@Test
	void readInvalidConfig_Content_and_Template() throws Exception {
		String yaml = "---\n" //
				+ "files:\n" //
				+ "  - path: test1.txt\n" //
				+ "    content: Hello World1\n" //
				+ "    template: /template.tpl" //
				+ "...";

		assertThrows(FilesConfigException.class, () -> FilesConfig.loadConfig(yaml));		
	}
}
