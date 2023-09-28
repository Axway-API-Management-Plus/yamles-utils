package com.axway.yamles.utils.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.axway.yamles.utils.helper.Audit;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Provides a Pebble function to lookup values from data sources.
 * 
 * @author mlook
 */
public abstract class LookupFunction implements Function {
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

	@Override
	public List<String> getArgumentNames() {
		List<FunctionArgument> funcArgs = this.provider.getFunctionArguments();
		List<String> args = new ArrayList<>(funcArgs.size());
		for (FunctionArgument a : funcArgs) {
			args.add(a.getName());
		}
		return args;
	}

	@Override
	public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
		Optional<String> value = lookup(args);

		if (!value.isPresent()) {
			throw new LookupFunctionException(this, "lookup key not found: " + argsToString(args));
		}

		Audit.AUDIT_LOG.info("  lookup: alias={} args=[{}]", this.alias, argsToString(args));

		return value.get();
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

	protected static String argsToString(Map<String, Object> args) {
		StringBuilder str = new StringBuilder();

		if (args != null) {
			args.forEach((key, value) -> {
				if (str.length() > 0) {
					str.append(", ");
				}
				str.append(key).append('=').append(value.toString());
			});
		}
		return str.toString();
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