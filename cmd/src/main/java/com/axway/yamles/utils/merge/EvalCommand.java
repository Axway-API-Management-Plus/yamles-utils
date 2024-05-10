package com.axway.yamles.utils.merge;

import java.io.PrintStream;
import java.util.Objects;

import com.axway.yamles.utils.plugins.Evaluator;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "eval", description = "Evalulates a template expression.", mixinStandardHelpOptions = true)
public class EvalCommand extends AbstractLookupEnabledCommand {

	@Parameters(index = "0", description = "Expression to be evaluated", arity = "1")
	private String expression;

	@Option(names = { "-n", "--new-line" }, description = "Add new line after printing", required = false)
	private boolean newLine = false;

	private final PrintStream out;

	public EvalCommand() {
		this(System.out);
	}

	EvalCommand(PrintStream out) {
		super();
		this.out = Objects.requireNonNull(out);
	}
	
	EvalCommand(PrintStream out, String expression, boolean newLine) {
		this(out);
		this.expression = Objects.requireNonNull(expression);
		this.newLine = newLine;
	}
	
	@Override
	public Integer call() throws Exception {
		initializeProviderManager(ExecutionMode.CONFIG);
		String result = Evaluator.eval(this.expression);

		if (this.newLine) {
			this.out.println(result);
		} else {
			this.out.print(result);
		}

		return 0;
	}
}
