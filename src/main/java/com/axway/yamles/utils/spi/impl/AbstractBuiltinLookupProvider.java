package com.axway.yamles.utils.spi.impl;

import java.util.Optional;

import com.axway.yamles.utils.spi.LookupFunction;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

public abstract class AbstractBuiltinLookupProvider extends AbstractLookupProvider {
	protected static final Optional<String> SOURCE = Optional.of("<built-in>");
	
	private boolean functionCreated = false;
	
	protected AbstractBuiltinLookupProvider() {
		super();
	}
	
	@Override
	public boolean isBuiltIn() {
		return true;
	}

	@Override
	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException {
		if (this.functionCreated) {
			throw new LookupProviderException(this, "built-in lookup function already created");
		}
		
		return buildFunction();
	}

	abstract protected LookupFunction buildFunction() throws LookupProviderException;
}
