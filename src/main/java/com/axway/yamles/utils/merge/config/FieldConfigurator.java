package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.plugins.ExecutionMode;

public class FieldConfigurator {
	private final ExecutionMode mode;

	private final List<FragmentSource> csl = new ArrayList<>();

	public FieldConfigurator(ExecutionMode mode) {
		this.mode = Objects.requireNonNull(mode, "configuration mode required");
	}

	public void setConfigFragmentFiles(List<File> files) {
		List<FragmentSource> csl = new ArrayList<>();

		if (files != null) {
			for (File f : files) {
				FragmentSource cs = FragmentSourceFactory.load(f);
				csl.add(cs);
			}
		}
		setConfigFragments(csl);
	}

	public void setConfigFragments(List<FragmentSource> csl) {
		this.csl.clear();

		if (csl == null)
			return;

		this.csl.addAll(csl);
	}

	public void apply(YamlEs es, boolean ignoreMissingValues) throws Exception {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Environmentalized Field Configuration");
		
		Objects.requireNonNull(es, "entity store project required");

		YamlEsConfig esConfig = new YamlEsConfig();
		esConfig.merge(csl);

		if (es.getRequiredValues().isPresent()) {
			if (!esConfig.allFieldsConfigured(es.getRequiredValues())) {
				if (!ignoreMissingValues) {
					throw new RuntimeException("Some required values are not configured. Check log for details!");
				}
			}
		}

		apply(es.getValuesFile(), esConfig);
	}

	public void apply(File out) throws Exception {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Environmentalized Field Configuration");

		YamlEsConfig esConfig = new YamlEsConfig();
		esConfig.merge(csl);

		apply(out, esConfig);
	}

	private void apply(File out, YamlEsConfig esConfig) throws Exception {
		esConfig.evalValues();

		if (out.getName().equals("-")) {
			System.out.println(esConfig.toYaml());
		} else {
			if (this.mode == ExecutionMode.CONFIG) {
				Yaml.write(out, esConfig.getConfig());
				Audit.AUDIT_LOG.info("values written to {}", out.getAbsoluteFile());
			} else {
				Audit.AUDIT_LOG.info("values skipped, due execution mode {}", this.mode);
			}
		}
	}
}
