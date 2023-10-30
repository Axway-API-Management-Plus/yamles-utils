package com.axway.yamles.utils.plugins;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides a Pebble function to lookup values from data sources.
 * 
 * @author mlook
 */
public abstract class LookupFunction {
	public static final String FUNCTION_PREFIX = "_";

	private final String alias;
	private final LookupProvider provider;
	private final Optional<String> definitionSource;

	/**
	 * Constructs a new lookup function.
	 * 
	 * @param alias    alias name of the lookup function
	 * @param provider provider constructed the function
	 * @param source   source where the lookup function is defined
	 */
	public LookupFunction(String alias, LookupProvider provider, Optional<String> source) {
		this.alias = Objects.requireNonNull(alias);
		this.provider = Objects.requireNonNull(provider);
		this.definitionSource = Objects.requireNonNull(source);
	}

	public String getAlias() {
		return this.alias;
	}

	public String getName() {
		return FUNCTION_PREFIX + this.alias;
	}

	public String getDefintionSource() {
		return this.definitionSource.orElse("<string>");
	}

	/**
	 * Returns the lookup provider implementing the function.
	 * 
	 * @return lookup provider
	 */
	public LookupProvider getProvider() {
		return this.provider;
	}

	abstract public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException;

	protected String getArg(FunctionArgument arg, Map<String, Object> args, String defaultValue) {
		Objects.requireNonNull(arg);
		Objects.requireNonNull(args);

		Object value = args.get(arg.getName());
		if (value == null) {
			if (arg.isRequired()) {
				throw new LookupFunctionException(this, "argument not passed to function: " + arg.getName());
			}
			value = defaultValue;
		}
		return value.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(alias);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LookupFunction other = (LookupFunction) obj;
		return Objects.equals(alias, other.alias);
	}
}