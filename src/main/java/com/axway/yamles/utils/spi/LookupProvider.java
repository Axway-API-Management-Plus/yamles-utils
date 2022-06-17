package com.axway.yamles.utils.spi;

import java.util.Optional;

import com.github.jknack.handlebars.Helper;

public interface LookupProvider extends Helper<String> {

	public String getName();
	
	public boolean isEnabled();

	public Optional<String> lookup(String key);
}
