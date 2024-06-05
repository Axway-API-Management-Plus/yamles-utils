package com.axway.yamles.utils.merge;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.plugins.Evaluator;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionConfigException;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.TemplateEngine;
import com.axway.yamles.utils.plugins.TemplateEngineException;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.attributes.methodaccess.MethodAccessValidator;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public class Mustache extends AbstractExtension implements TemplateEngine {

	private static class LookupFunctionWrapper implements Function {
		private final LookupFunction lf;

		public LookupFunctionWrapper(LookupFunction lf) {
			this.lf = Objects.requireNonNull(lf, "lookup function required");
		}

		public LookupFunction getFunction() {
			return this.lf;
		}

		@Override
		public List<String> getArgumentNames() {
			List<FunctionArgument> funcArgs = this.lf.getProvider().getFunctionArguments();
			List<String> args = new ArrayList<>(funcArgs.size());
			for (FunctionArgument a : funcArgs) {
				args.add(a.getName());
			}
			return args;
		}

		@Override
		public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
				int lineNumber) {
			Optional<String> value = this.lf.lookup(args);
			if (!value.isPresent()) {
				throw new LookupFunctionException(this.lf, "lookup key not found: " + argsToString(args));
			}

			Audit.AUDIT_LOG.info("  lookup: alias={} args=[{}]", this.lf.getAlias(), argsToString(args));

			return value.get();
		}

		private String argsToString(Map<String, Object> args) {
			StringBuilder str = new StringBuilder();

			if (args != null) {
				args.forEach((key, value) -> {
					Optional<FunctionArgument> fa = this.lf.getProvider().getFunctionArgumentByName(key);
					if (str.length() > 0) {
						str.append(", ");
					}
					boolean mask = (!fa.isPresent() || fa.get().isSecret());
					str.append(key).append('=').append(mask ? "*****" : value.toString());
				});
			}
			return str.toString();
		}
	}

	/**
	 * Method access validator to disable all method access.
	 * 
	 * <p>
	 * A vulnerability report
	 * <a href="https://nvd.nist.gov/vuln/detail/CVE-2022-37767">CVE-2022-37767</a>
	 * exists for the Pebble engine. According to this CVE any method can be called
	 * by default which could execute code on the server running the Pebble
	 * Template.
	 * </p>
	 * <p>
	 * This is a security issue as the configuration may not provided by the same
	 * team running the YAML-ES utility. In the context of the YAML-ES utility, this
	 * feature is not required. Therefore, to avoid this issue, the execution of
	 * Java methods will be disabled.
	 * <p>
	 * The Pebble Template engine supports method access validation, so that methods
	 * can be checked before execution.
	 * </p>
	 * <p>
	 * This validator rejects the access to all methods.
	 * </p>
	 */
	static class DisabledMethodAceess implements MethodAccessValidator {
		@Override
		public boolean isMethodAccessAllowed(Object object, Method method) {
			return false;
		}

	}

	private static final Logger log = LogManager.getLogger(Mustache.class);

	private final Map<String, Function> functions = new TreeMap<>();
	private PebbleEngine pe = null;

	private static Mustache instance;

	private static PebbleEngine.Builder basicPebbleEngineBuilder() {
		return new PebbleEngine.Builder() //
				.autoEscaping(false) //
				.strictVariables(true) //
				.methodAccessValidator(new DisabledMethodAceess());
	}

	public static Mustache getInstance() {
		synchronized (Mustache.class) {
			if (instance == null) {
				instance = new Mustache();
				instance.refresh();
			}
			Evaluator.setTemplateEngine(instance);
		}
		return instance;
	}

	Mustache() {
		this.pe = basicPebbleEngineBuilder().build();
	}

	@Override
	public String evaluate(String template) throws TemplateEngineException {
		if (pe == null) {
			throw new IllegalStateException("Pebble engine not initialized");
		}
		PebbleTemplate pt = pe.getLiteralTemplate(template);
		return evaluate(pt);
	}

	@Override
	public String evaluate(File template) throws TemplateEngineException {
		if (pe == null) {
			throw new IllegalStateException("Pebble engine not initialized");
		}
		PebbleTemplate pt = this.pe.getTemplate(template.getAbsolutePath());
		return evaluate(pt);
	}

	private String evaluate(PebbleTemplate pt) {
		StringWriter result = new StringWriter();
		try {
			pt.evaluate(result);
		} catch (IOException e) {
			throw new TemplateEngineException("error on evaluating template", e);
		}

		return result.toString();
	}

	@Override
	public Map<String, Function> getFunctions() {
		return this.functions;
	}

	public void addFunction(LookupFunction lf) {
		LookupFunctionWrapper existingFunc = (LookupFunctionWrapper) this.functions.put(lf.getName(),
				new LookupFunctionWrapper(lf));
		if (existingFunc != null)
			throw new LookupFunctionConfigException(lf.getDefintionSource(), "alias '" + lf.getAlias()
					+ "' already defined in " + existingFunc.getFunction().getDefintionSource());
		Audit.AUDIT_LOG.info("lookup function registered: func={}; provider={}; source={}", lf.getName(),
				lf.getProvider().getName(), lf.getDefintionSource());
		refresh();
	}

	public void clearFunctions() {
		this.functions.clear();
		log.debug("Pebble functions cleared");
		refresh();
	}

	public Collection<LookupFunction> getLookupFunctions() {
		List<LookupFunction> lfs = new ArrayList<>();
		this.functions.forEach((name, func) -> {
			lfs.add(((LookupFunctionWrapper) func).getFunction());
		});
		return lfs;
	}

	private final void refresh() {
		this.pe = basicPebbleEngineBuilder() //
				.extension(this) //
				.build();
		log.debug("Pebble template engine initialized");
	}
}
