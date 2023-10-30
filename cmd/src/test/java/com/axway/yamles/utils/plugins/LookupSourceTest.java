package com.axway.yamles.utils.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.merge.ProviderManager;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;
import com.axway.yamles.utils.test.MapLookupProvider;

public class LookupSourceTest {
	
	private static final MapLookupProvider mlp = new MapLookupProvider();
	
	@BeforeAll
	static void initLookupManager() {
		ProviderManager pm = ProviderManager.initialize(ExecutionMode.CONFIG);
		pm.removeProvider(mlp.getName());
		pm.addProvider(mlp);
		pm.configureBuiltInFunction();
	}

	@Test
	void eval_parameters_from_map() {
		ConfigParameter param = new ConfigParameter("param", true, "", Type.string, true);

		final String KEY = "lookup_source_param";
		final String TEMPLATE = "{{ _map('" + KEY + "') }}";
		final String VALUE = "value";

		mlp.getMap().put(KEY, VALUE);

		Map<String, String> params = new HashMap<>();
		params.put(param.getName(), TEMPLATE);

		LookupSource src = new LookupSource("provider", params);

		assertEquals(VALUE, src.getConfig(param, ""));
	}

	@Test
	void eval_parameters_from_invalid_map() {
		ConfigParameter param = new ConfigParameter("param", true, "", Type.string, true);

		final String KEY = "non_existing_prop";
		final String TEMPLATE = "{{ _map('" + KEY + "') }}";

		Map<String, String> params = new HashMap<>();
		params.put("param", TEMPLATE);

		LookupSource src = new LookupSource("provider", params);
		assertThrows(LookupFunctionException.class, () -> src.getConfig(param, ""));
	}

	@Test
	void dont_eval_parameters_without_mustache_support() {
		ConfigParameter paramNoEval = new ConfigParameter("param", true, "", Type.string, false);

		final String KEY = "lookup_source_param";
		final String TEMPLATE = "{{ _map('" + KEY + "') }}";
		final String VALUE = "value";

		mlp.getMap().put(KEY, VALUE);

		Map<String, String> params = new HashMap<>();
		params.put(paramNoEval.getName(), TEMPLATE);

		LookupSource src = new LookupSource("provider", params);

		assertEquals(TEMPLATE, src.getConfig(paramNoEval, ""));
	}
}
