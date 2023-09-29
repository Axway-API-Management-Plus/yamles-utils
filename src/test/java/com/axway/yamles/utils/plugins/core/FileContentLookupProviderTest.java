package com.axway.yamles.utils.plugins.core;

import java.io.File;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.merge.LookupManager;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupSource;

public class FileContentLookupProviderTest {
	
	@BeforeAll
	static void initLookupManager() {
		LookupManager.getInstance();
	}


	@Test
	void testTextContent() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("content.txt").getFile());
        
        FileContentLookupProvider lp = new FileContentLookupProvider();
        
		LookupSource ls = new LookupSource("test", lp.getName(), Collections.emptyMap());
		ls.setConfigSource(new File(file.getParentFile(), "content.txt"));
		
		LookupFunction lf = lp.buildFunction(ls);
		
        String expectedContent = FileUtils.readFileToString(file,  "UTF-8");
        
        Map<String, Object> args = new HashMap<>();
        args.put(FileContentLookupProvider.ARG_KEY.getName(), "content.txt");
        args.put(FileContentLookupProvider.ARG_ENCODING.getName(), "UTF-8");

        String lookupContent = lf.lookup(args).get();
 
        Assertions.assertEquals(expectedContent, lookupContent);
	}
	
	@Test
	void testBinaryContent() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("content.bin").getFile());
        
        FileContentLookupProvider lp = new FileContentLookupProvider();
        
		LookupSource ls = new LookupSource("test", lp.getName(), Collections.emptyMap());
		ls.setConfigSource(new File(file.getParentFile(), "content.bin"));
		
		LookupFunction lf = lp.buildFunction(ls);
		
        byte[] expectedContent = FileUtils.readFileToByteArray(file);
        
        Map<String, Object> args = new HashMap<>();
        args.put(FileContentLookupProvider.ARG_KEY.getName(), "content.bin");
        args.put(FileContentLookupProvider.ARG_ENCODING.getName(), "binary");

        String lookupContentB64 = lf.lookup(args).get();
        byte[] lookupContent = Base64.getDecoder().decode(lookupContentB64);
 
        Assertions.assertArrayEquals(expectedContent, lookupContent);
	}
}
