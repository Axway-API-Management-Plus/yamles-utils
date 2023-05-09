package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.JsonDoc;
import com.axway.yamles.utils.spi.LookupProviderException;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class JsonLookupProvider extends AbstractJsonDocLookupProvider {

	private static final Logger log = LogManager.getLogger(JsonLookupProvider.class);

	@Option(names = { "--lookup-json" }, description = "path to an JSON file", paramLabel = "FILE")
	private List<File> jsonFiles;
	
	public JsonLookupProvider() {
		super(log);
	}

	@Override
	public String getName() {
		return "json";
	}

	@Override
	public boolean isEnabled() {
		return this.jsonFiles != null && !this.jsonFiles.isEmpty();
	}

	@Override
	public void onRegistered() {
		synchronized (this) {
			if (isEmpty()) {
				for (File file : this.jsonFiles) {
					try {
						JsonDoc doc = new JsonDoc(file);
						add(doc);
						log.info("JSON lookup file registered: {}", doc.getName());
					} catch (Exception e) {
						throw new LookupProviderException(this,
								"error on loading lookup JSON file: " + file.getAbsolutePath(), e);
					}
				}
			}
		}
	}
}
