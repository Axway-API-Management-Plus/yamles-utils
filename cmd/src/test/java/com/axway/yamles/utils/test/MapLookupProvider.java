package com.axway.yamles.utils.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.axway.yamles.utils.plugins.AbstractBuiltinLookupProvider;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.LookupProviderException;

public class MapLookupProvider extends AbstractBuiltinLookupProvider {
	private final Map<String, String> map = new HashMap<String, String>();

	protected static class LF extends LookupFunction {
		private final Map<String, String> map;

		public LF(String alias, MapLookupProvider provider, Map<String, String> map) {
			super(alias, provider, AbstractBuiltinLookupProvider.SOURCE);
			this.map = map;
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			String key = getArg(ARG_KEY, args, "");
			if (key == null || key.isEmpty()) {
				return Optional.empty();
			}

			String value = this.map.get(key);
			if (value == null) {
				return Optional.empty();
			}
			return Optional.of(value);
		}
	}

	protected static FunctionArgument ARG_KEY = new FunctionArgument("key", true, "Name of map key");

	public MapLookupProvider() {
		super();
		add(ARG_KEY);
	}

	@Override
	public String getName() {
		return "map";
	}

	@Override
	public String getSummary() {
		return "Lookup values from a map.";
	}

	@Override
	public String getDescription() {
		return "The key represents the name of the map entry.";
	}

	@Override
	protected LookupFunction buildFunction() throws LookupProviderException {
		return new LF(getName(), this, this.map);
	}

	public Map<String, String> getMap() {
		return this.map;
	}
}
