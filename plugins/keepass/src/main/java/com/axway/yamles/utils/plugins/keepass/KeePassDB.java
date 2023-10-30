package com.axway.yamles.utils.plugins.keepass;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleEntry;
import org.linguafranca.pwdb.kdbx.simple.SimpleGroup;

class KeePassDB {

	public static class EntryPath {
		private final String[] groupPath;
		private final String entry;

		public EntryPath(String path) {
			Objects.requireNonNull(path, "KeePass entry path requried");
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			String[] parts = Objects.requireNonNull(path, "KeePass entry path required").split("/");
			if (parts.length == 0) {
				throw new IllegalArgumentException("KeePass entry path is empty");
			}
			this.entry = parts[parts.length - 1];
			this.groupPath = new String[parts.length - 1];
			for (int i = 0; i < parts.length - 1; i++) {
				this.groupPath[i] = parts[i];
			}
		}

		String[] getGroupsPath() {
			return this.groupPath;
		}

		String getEntry() {
			return this.entry;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			for (String g : this.groupPath) {
				str.append('/').append(g);
			}
			str.append('/').append(this.entry);
			return str.toString();
		}
	}

	private SimpleDatabase db;

	public KeePassDB(File db, String passphrase, File keyFile) throws Exception {
		this(new FileInputStream(Objects.requireNonNull(db, "KeePass database file is null")), //
				passphrase, //
				(keyFile != null) ? new FileInputStream(keyFile) : null //
		);
	}

	public KeePassDB(InputStream in, String passphrase, InputStream keyFile) throws Exception {
		Objects.requireNonNull(passphrase, "passphrase is null; must be empty if not requried");
		KdbxCreds credentials;
		if (keyFile == null) {
			credentials = new KdbxCreds(passphrase.getBytes());
		} else {
			credentials = new KdbxCreds(passphrase.getBytes(), keyFile);
		}
		this.db = SimpleDatabase.load(credentials, in);
	}

	public Optional<String> getUser(EntryPath ep) {
		SimpleEntry entry = getEntry(ep);
		if (entry != null && entry.getUsername() != null) {
			return Optional.of(entry.getUsername());
		}
		return Optional.empty();
	}

	public Optional<String> getPassword(EntryPath ep) {
		SimpleEntry entry = getEntry(ep);
		if (entry != null && entry.getPassword() != null) {
			return Optional.of(entry.getPassword());
		}
		return Optional.empty();
	}
	
	public Optional<String> getURL(EntryPath ep) {
		SimpleEntry entry = getEntry(ep);
		if (entry != null && entry.getUrl() != null) {
			return Optional.of(entry.getUrl());
		}
		return Optional.empty();
	}

	public Optional<String> getProperty(EntryPath ep, String name) {
		SimpleEntry entry = getEntry(ep);
		if (entry != null) {
			String value = entry.getProperty(name);
			if (value != null) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

	public Optional<byte[]> getBinaryProperty(EntryPath ep, String name) {
		SimpleEntry entry = getEntry(ep);
		if (entry != null) {
			byte[] value = entry.getBinaryProperty(name);
			if (value != null) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

	private SimpleEntry getEntry(EntryPath ep) {
		SimpleGroup group = db.getRootGroup();
		for (String groupName : ep.getGroupsPath()) {
			group = getGroup(group, groupName);
			if (group == null) {
				return null;
			}
		}
		return getEntry(group, ep.getEntry());
	}

	private SimpleGroup getGroup(SimpleGroup parent, String name) {
		for (SimpleGroup group : parent.getGroups()) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		return null;
	}

	private SimpleEntry getEntry(SimpleGroup parent, String name) {
		for (SimpleEntry entry : parent.getEntries()) {
			if (entry.getTitle().equals(name)) {
				return entry;
			}
		}
		return null;
	}
}
