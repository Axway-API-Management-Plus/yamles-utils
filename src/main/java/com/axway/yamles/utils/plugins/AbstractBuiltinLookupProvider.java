package com.axway.yamles.utils.plugins;

import java.util.Optional;

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
