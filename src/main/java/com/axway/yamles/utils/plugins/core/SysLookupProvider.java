package com.axway.yamles.utils.plugins.core;

import java.util.Map;
import java.util.Optional;

import com.axway.yamles.utils.plugins.AbstractBuiltinLookupProvider;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.LookupProviderException;

/**
 * Lookup provider for system properties.
 * 
 * @author mlook
 */
public class SysLookupProvider extends AbstractBuiltinLookupProvider {
	protected static class LF extends LookupFunction {
		public LF(String alias, SysLookupProvider provider) {
			super(alias, provider, AbstractBuiltinLookupProvider.SOURCE);
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			String key = getArg(ARG_KEY, args, "");
			if (key == null || key.isEmpty()) {
				return Optional.empty();
			}

			String value = System.getProperty(key);
			if (value == null) {
				return Optional.empty();
			}
			return Optional.of(value);
		}
	}

	protected static FunctionArgument ARG_KEY = new FunctionArgument("key", true, "Name of system property");

	public SysLookupProvider() {
		super();
		add(ARG_KEY);
	}

	@Override
	public String getName() {
		return "sys";
	}

	@Override
	public String getSummary() {
		return "Lookup values from system properties.";
	}

	@Override
	public String getDescription() {
		return "The key represents the name of the system property.";
	}

	@Override
	protected LookupFunction buildFunction() throws LookupProviderException {
		return new LF(getName(), this);
	}
}
