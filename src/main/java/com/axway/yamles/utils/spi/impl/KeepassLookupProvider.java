package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.KeePassDB;
import com.axway.yamles.utils.helper.KeePassDB.EntryPath;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class KeepassLookupProvider extends AbstractLookupProvider {
	private static final Logger log = LogManager.getLogger(KeepassLookupProvider.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final Charset ISO8859 = Charset.forName("ISO-8859-1");

	static enum What {
		user(false), password(false), prop(true), binUTF8(true) {
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

		private final boolean fieldRequired;

		private What(boolean fieldRequired) {
			this.fieldRequired = fieldRequired;
		}

		public boolean isFieldRequired() {
			return this.fieldRequired;
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

		private Key(EntryPath ep, What what, String pname) {
			this.ep = Objects.requireNonNull(ep);
			this.what = Objects.requireNonNull(what);
			this.pname = pname;
		}

		static Key parse(String key) {
			if (key == null || key.isEmpty()) {
				throw new IllegalArgumentException("KeePass key - null or empty");
			}

			int idx = key.lastIndexOf(':');
			if (idx < 0 || idx == key.length() - 1) {
				throw new IllegalArgumentException("KeePass key - missing value (behind ':'): key= " + key);
			}
			String path = key.substring(0, idx);
			String value = key.substring(idx + 1);

			What what;
			String pname = null;

			String[] parts = value.split("\\.", 2);
			try {
				what = What.valueOf(parts[0]);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("KeePass key - invalid value: key=" + key + "; value=" + parts[0]);
			}
			if (!what.isFieldRequired() && parts.length > 1) {
				throw new IllegalArgumentException(
						"KeePass key - field not supported: key=" + key + "; value=" + parts[0]);
			}
			if (parts.length > 1) {
				pname = parts[1];
			}
			if (what.isFieldRequired() && (pname == null || pname.isEmpty())) {
				throw new IllegalArgumentException("KeePass key - missing field: key=" + key);
			}

			return new Key(new EntryPath(path), what, pname);
		}
	}

	static class Kdb {
		@Option(names = "--kdb", description = "KeePass DB file", paramLabel = "FILE", required = true)
		File dbFile;

		@Option(names = "--kdb-pass", description = "master passphrase for KeePass DB", required = false)
		String passphrase;

		@Option(names = "--kdb-key-file", description = "key file for KeePass DB", paramLabel = "FILE", required = false)
		File keyFile;

		KeePassDB db = null;

		Optional<String> getValue(String key) throws Exception {
			synchronized (this) {
				if (db == null) {
					db = new KeePassDB(this.dbFile, this.passphrase, this.keyFile);
				}
			}
			Key k = Key.parse(key);
			return getValue(k);
		}

		private Optional<String> getValue(Key key) {
			Optional<String> value;

			switch (key.what) {
			case user:
				value = db.getUser(key.ep);
				break;
			case password:
				value = db.getPassword(key.ep);
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
			return this.dbFile.getAbsolutePath();
		}
	}

	@ArgGroup(exclusive = false)
	List<Kdb> kdbs;

	@Override
	public String getName() {
		return "keepass";
	}

	@Override
	public boolean isEnabled() {
		return this.kdbs != null && !this.kdbs.isEmpty();
	}

	@Override
	public Optional<String> lookup(String key) {
		if (!isEnabled())
			return Optional.empty();

		Key k = Key.parse(key);

		Optional<String> result = Optional.empty();
		for (Kdb kdb : this.kdbs) {
			Optional<String> value = kdb.getValue(k);
			if (value.isPresent()) {
				if (!result.isPresent()) {
					log.debug("found lookup key '{}' in {}", key, kdb);
				} else {
					log.debug("overwrite lookup key '{}' by {}", key, kdb);
				}
				result = value;
			}
		}
		return result;
	}
}
