package com.axway.yamles.utils.plugins;

import java.util.Map;
import java.util.Optional;

/**
 * Lookup function which always returns an empty string.
 * 
 * <p>
 * This lookup function is used for the {@link ExecutionMode#SYNTAX_CHECK}
 * execution mode. Lookup provides can return this function in the as dummy
 * function for the syntax check.
 * </p>
 * <p>
 * The function internally, checks for required arguments.
 * </p>
 * 
 * @see LookupProvider#buildFunction(LookupSource)
 */
public class EmptyValueLookupFunction extends LookupFunction {

	private final Optional<String> value = Optional.of("");

	public EmptyValueLookupFunction(String alias, LookupProvider provider, Optional<String> source) {
		super(alias, provider, source);
	}

	/**
	 * Lookup a value.
	 * 
	 * <p>
	 * Internally, it checks the provided function arguments.
	 * </p>
	 * 
	 * @return always returns an empty string
	 */
	@Override
	public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
		checkArgs(args);
		return this.value;
	}

	protected void checkArgs(Map<String, Object> args) throws LookupFunctionException {
		getProvider().getFunctionArguments().forEach(fa -> {
			getArg(fa, args, "");
		});
	}
}
