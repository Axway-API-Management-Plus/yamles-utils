package com.axway.yamles.utils.plugins.keepass;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pf4j.Extension;

import com.axway.yamles.utils.plugins.AbstractLookupProvider;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.LookupProviderException;
import com.axway.yamles.utils.plugins.LookupSource;
import com.axway.yamles.utils.plugins.keepass.KeePassDB.EntryPath;

@Extension
public class KeepassLookupProvider extends AbstractLookupProvider {
	public static final ConfigParameter CFG_PARAM_FILE = new ConfigParameter("kdb", true, "Path to KeePass DB file",
			ConfigParameter.Type.file, false);
	public static final ConfigParameter CFG_PARAM_PASS = new ConfigParameter("passphrase", true,
			"Passsphrase of KeePass DB", ConfigParameter.Type.string, true);
	public static final ConfigParameter CFG_PARAM_KEY_FILE = new ConfigParameter("key", false,
			"Path to master key file", ConfigParameter.Type.file, false);

	public static final FunctionArgument ARG_KEY = new FunctionArgument("key", true, "Path to entry");
	public static final FunctionArgument ARG_WHAT = new FunctionArgument("what", true,
			"Field to be retrieved from the KeePass entry");
	public static final FunctionArgument ARG_PNAME = new FunctionArgument("pname", false,
			"Name of property within field");

	private static final Logger log = LogManager.getLogger(KeepassLookupProvider.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final Charset ISO8859 = Charset.forName("ISO-8859-1");

	static enum What {
		user(false), password(false), url(false), prop(true), binUTF8(true) {
			@Override
			public Optional<String> toString(Optional<byte[]> value) {
				if (Objects.requireNonNull(value).isPresent() && !(value.get() instanceof byte[])) {
					throw new IllegalArgumentException("Optional<byte[]> expected");
				}
				if (!value.isPresent()) {
					return Optional.empty();
				}
				return Optional.of(new String((byte[]) value.get(), UTF8));
			}
		},
		binISO8859(true) {
			@Override
			public Optional<String> toString(Optional<byte[]> value) {
				if (Objects.requireNonNull(value).isPresent() && !(value.get() instanceof byte[])) {
					throw new IllegalArgumentException("Optional<byte[]> expected");
				}
				if (!value.isPresent()) {
					return Optional.empty();
				}
				return Optional.of(new String((byte[]) value.get(), ISO8859));
			}
		},
		binB64(true) {
			@Override
			public Optional<String> toString(Optional<byte[]> value) {
				if (Objects.requireNonNull(value).isPresent() && !(value.get() instanceof byte[])) {
					throw new IllegalArgumentException("Optional<byte[]> expected");
				}
				if (!value.isPresent()) {
					return Optional.empty();
				}
				return Optional.of(Base64.getEncoder().encodeToString((byte[]) value.get()));
			}
		};

		private final boolean propertyNameRequired;

		private What(boolean propertyNameRequired) {
			this.propertyNameRequired = propertyNameRequired;
		}

		public boolean isPropertyNameRequired() {
			return this.propertyNameRequired;
		}

		public Optional<String> toString(Optional<byte[]> value) {
			throw new UnsupportedOperationException(
					"Optional<byte[]> to Optional<String> not implemented for " + this.name());
		}
	}

	static class Key {
		final EntryPath ep;
		final What what;
		final String pname;

		Key(EntryPath ep, What what, String pname) {
			this.ep = Objects.requireNonNull(ep);
			this.what = Objects.requireNonNull(what);
			this.pname = pname;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("path=").append(this.ep);
			str.append("; ");
			str.append("what=").append(this.what);
			str.append("; ");
			str.append("pname=").append(this.pname);
			return str.toString();
		}
	}

	static class Kdb {
		private final KeePassDB db;
		private final String source;

		Kdb(String alias, File dbFile, String passphrase, Optional<File> keyFile) throws Exception {
			this.source = Objects.requireNonNull(dbFile).getAbsolutePath();
			this.db = new KeePassDB(dbFile, Objects.requireNonNull(passphrase, "passphrase required"),
					keyFile.isPresent() ? keyFile.get() : null);
		}

		Kdb(String alias, KeePassDB db) {
			this.db = Objects.requireNonNull(db);
			this.source = "<undefined>";
		}

		Optional<String> getValue(Key key) throws Exception {
			Optional<String> value;

			switch (key.what) {
			case user:
				value = db.getUser(key.ep);
				break;
			case password:
				value = db.getPassword(key.ep);
				break;
			case url:
				value = db.getURL(key.ep);
				break;
			case prop:
				value = db.getProperty(key.ep, key.pname);
				break;
			case binB64:
			case binISO8859:
			case binUTF8:
				value = key.what.toString(db.getBinaryProperty(key.ep, key.pname));
				break;

			default:
				throw new UnsupportedOperationException("'" + key.what.name() + "' not implemented for KeePass lookup");
			}
			return value;
		}

		@Override
		public String toString() {
			return this.source;
		}
	}

	static class LF extends LookupFunction {
		private final Kdb kdb;

		public LF(String alias, KeepassLookupProvider provider, Optional<String> source, Kdb kdb) {
			super(alias, provider, source);
			this.kdb = Objects.requireNonNull(kdb, "KeePass DB requried");
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			Optional<String> result = Optional.empty();

			EntryPath ep = new EntryPath(getArg(ARG_KEY, args, ""));
			What what = What.valueOf(getArg(ARG_WHAT, args, ""));
			String pname = null;

			if (what.isPropertyNameRequired()) {
				pname = getArg(ARG_PNAME, args, "");
			}

			Key key = new Key(ep, what, pname);

			try {
				log.debug("search for KeePass key: {}", key);
				result = kdb.getValue(key);
				if (result.isPresent()) {
					log.debug("found lookup key: provider={}; alias={}; source={}; key={}", getProvider().getName(),
							getAlias(), kdb.source, key);
				}
			} catch (Exception e) {
				throw new LookupFunctionException(this, "error on lookup key: alias=" + getAlias() + "; key=" + key, e);
			}
			return result;
		}
	}

	public KeepassLookupProvider() {
		super();
		add(ARG_KEY, ARG_WHAT, ARG_PNAME);
		add(CFG_PARAM_FILE, CFG_PARAM_PASS, CFG_PARAM_KEY_FILE);
	}

	@Override
	public String getName() {
		return "keepass";
	}

	@Override
	public String getSummary() {
		return "Lookup values from KeePass databases.";
	}

	@Override
	public String getDescription() {
		return "The key represents the entry in the KeePass DB.";
	}

	@Override
	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException {
		File dbFile = source.getFileFromConfig(CFG_PARAM_FILE).get();
		String passphrase = source.getConfig(CFG_PARAM_PASS, "");
		Optional<File> keyFile = source.getFileFromConfig(CFG_PARAM_KEY_FILE);
		
		Optional<LookupFunction> clf = checkOnlyLookupFunction(source);
		if (clf.isPresent())
			return clf.get();

		try {
			Kdb kdb = new Kdb(source.getAlias(), dbFile, passphrase, keyFile);
			return new LF(source.getAlias(), this, source.getConfigSource(), kdb);
		} catch (Exception e) {
			throw new LookupProviderException(this, "error on initializing KeePass DB: " + dbFile.getAbsolutePath(), e);
		}
	}
}
