package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.KeePassDB;
import com.axway.yamles.utils.helper.KeePassDB.EntryPath;
import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

public class KeepassLookupProvider extends AbstractLookupProvider {
	public static final ConfigParameter CFG_PARAM_FILE = new ConfigParameter("kdb", true, "Path to KeePass DB file",
			ConfigParameter.Type.file);
	public static final ConfigParameter CFG_PARAM_PASS = new ConfigParameter("passphrase", true,
			"Passsphrase of KeePass DB", ConfigParameter.Type.string);
	public static final ConfigParameter CFG_PARAM_KEY_FILE = new ConfigParameter("key", false,
			"Path to master key file", ConfigParameter.Type.file);

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
		private final String alias;
		private final KeePassDB db;
		private final String source;

		Kdb(String alias, File dbFile, String passphrase, Optional<File> keyFile) throws Exception {
			this.alias = Objects.requireNonNull(alias);
			this.source = Objects.requireNonNull(dbFile).getAbsolutePath();
			this.db = new KeePassDB(dbFile, Objects.requireNonNull(passphrase, "passphrase required"),
					keyFile.isPresent() ? keyFile.get() : null);
		}

		Kdb(String alias, KeePassDB db) {
			this.alias = Objects.requireNonNull(alias);
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

	private Map<String, Kdb> kdbs = new HashMap<>();

	public KeepassLookupProvider() {
		super("path to entry");
		add(ARG_WHAT, ARG_PNAME);
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
	public boolean isEnabled() {
		return this.kdbs != null && !this.kdbs.isEmpty();
	}

	@Override
	public void addSource(LookupSource source) throws LookupProviderException {
		File dbFile = source.getFileFromRequiredParam(CFG_PARAM_FILE.getName());
		String passphrase = source.getRequiredParam(CFG_PARAM_PASS.getName());
		Optional<File> keyFile = source.getFileFromParam(CFG_PARAM_KEY_FILE.getName());

		try {
			Kdb kdb = new Kdb(source.getAlias(), dbFile, passphrase, keyFile);
			if (this.kdbs.put(kdb.alias, kdb) != null) {
				throw new LookupProviderException(this, "KeePass DB alias already exists: alias=" + kdb.alias);
			}
		} catch (Exception e) {
			throw new LookupProviderException(this, "error on initializing KeePass DB: " + dbFile.getAbsolutePath(), e);
		}
		log.debug("KeePass lookup DB registered: {}", dbFile.getAbsolutePath());
	}

	@Override
	public Optional<String> lookup(String alias, Map<String, Object> args) {
		Optional<String> result = Optional.empty();
		
		Kdb kdb = this.kdbs.get(alias);
		if (kdb == null) {
			log.error("Keepass DB alias not found: provider={}; alias={}", getName(), alias);
			return result;
		}
		
		EntryPath ep = new EntryPath(getStringArg(args, ARG_KEY.getName()));
		What what = What.valueOf(getStringArg(args, ARG_WHAT.getName()));
		String pname = null;

		if (what.isPropertyNameRequired()) {
			pname = getStringArg(args, ARG_PNAME.getName());
		}

		Key key = new Key(ep, what, pname);

		try {
			log.debug("search for KeePass key: {}", key);
			result = kdb.getValue(key);
			if (result.isPresent()) {
				log.debug("found lookup key: provider={}; alias={}; source={}; key={}", getName(), kdb.alias, kdb.source, key);
			}
		} catch (Exception e) {
			throw new LookupProviderException(this, "error on lookup key: alias=" + alias + "; key=" + key, e);
		}
		return result;
	}
}
