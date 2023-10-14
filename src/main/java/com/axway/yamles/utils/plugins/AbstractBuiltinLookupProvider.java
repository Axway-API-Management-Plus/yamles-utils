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

		Optional<LookupFunction> clf = checkOnlyLookupFunction(source);
		if (clf.isPresent())
			return clf.get();

		return buildFunction();
	}

	@Override
	protected Optional<LookupFunction> checkOnlyLookupFunction(LookupSource source) {
		if (getMode() == ExecutionMode.SYNTAX_CHECK) {
			return Optional.of(new EmptyValueLookupFunction(getName(), this, SOURCE));
		}
		return Optional.empty();
	}

	abstract protected LookupFunction buildFunction() throws LookupProviderException;
}
