package com.axway.yamles.utils.spi.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.JsonDoc;
import com.axway.yamles.utils.spi.LookupProviderException;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class EnvJsonLookupProvider extends AbstractJsonDocLookupProvider {

	private static final Logger log = LogManager.getLogger(EnvJsonLookupProvider.class);

	@Option(names = {
			"--lookup-envjson" }, description = "environment variable containing a JSON document", paramLabel = "NAME")
	private List<String> names;

	public EnvJsonLookupProvider() {
		super(log);
	}

	@Override
	public String getName() {
		return "envjson";
	}

	@Override
	public boolean isEnabled() {
		return this.names != null && !this.names.isEmpty();
	}

	@Override
	public void onRegistered() {
		synchronized (this) {
			if (isEmpty()) {
				for (String name : this.names) {
					try {
						String json = System.getenv(name);
						if (json == null) {
							throw new RuntimeException("environment variable not found: " + name);
						}
						JsonDoc doc = new JsonDoc(name, json);
						add(doc);
						log.info("JSON lookup from environment var registered: {}", doc.getName());
					} catch (Exception e) {
						throw new LookupProviderException(this,
								"error on initialize JSON lookup from environment variable: " + name, e);
					}
				}
			}
		}
	}
}
