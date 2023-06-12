package com.axway.yamles.utils.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.KeePassDB.EntryPath;

public class KeePassDBTest {
	
	public static final String PATH = "/keepass.kdbx";

	public static InputStream getTestKDBInputStream() {
		InputStream in = KeePassDBTest.class.getResourceAsStream(PATH);
		if (in == null) {
			throw new IllegalStateException("'/keepass.kdbx' not found in classpath");
		}
		return in;
	}

	public static KeePassDB getTestKDB() throws Exception {
		return new KeePassDB(getTestKDBInputStream(), "changeme", null);
	}

	@Test
	void openDatabase() throws Exception {
		getTestKDB();
	}

	@Test
	void dontOpenDatabaseOnWrongPassphrase() {
		assertThrows(Exception.class, () -> {
			new KeePassDB(getTestKDBInputStream(), "wrong", null);
		});
	}

	@Test
	void createEntryPath() {
		EntryPath ep;
		ep = new EntryPath("/group/subgroup/entry");
		assertEquals(2, ep.getGroupsPath().length);
		assertEquals("group", ep.getGroupsPath()[0]);
		assertEquals("subgroup", ep.getGroupsPath()[1]);
		assertEquals("entry", ep.getEntry());

		ep = new EntryPath("group/subgroup/entry");
		assertEquals(2, ep.getGroupsPath().length);
		assertEquals("group", ep.getGroupsPath()[0]);
		assertEquals("subgroup", ep.getGroupsPath()[1]);
		assertEquals("entry", ep.getEntry());

		ep = new EntryPath("entry");
		assertEquals(0, ep.getGroupsPath().length);
		assertEquals("entry", ep.getEntry());
	}

	@Test
	void entryPathToString() {
		assertEquals("/group/subgroup/entry", new EntryPath("/group/subgroup/entry").toString());
		assertEquals("/group/subgroup/entry", new EntryPath("group/subgroup/entry").toString());
		assertEquals("/entry", new EntryPath("entry").toString());
	}

	@Test
	void getUser() throws Exception {
		KeePassDB kdb = getTestKDB();

		EntryPath ep;
		Optional<String> user;

		ep = new EntryPath("/generic-user");
		user = kdb.getUser(ep);
		assertTrue(user.isPresent());
		assertEquals("user", user.get());

		ep = new EntryPath("/General/generic-user-general");
		user = kdb.getUser(ep);
		assertTrue(user.isPresent());
		assertEquals("user", user.get());
	}

	@Test
	void getUserFromNonExistingEntry() throws Exception  {
		KeePassDB kdb = getTestKDB();
		EntryPath ep;
		Optional<String> user;

		ep = new EntryPath("/no-user");
		user = kdb.getUser(ep);
		assertFalse(user.isPresent());
	}

	@Test
	void getPassword() throws Exception  {
		KeePassDB kdb = getTestKDB();
		EntryPath ep;
		Optional<String> pwd;

		ep = new EntryPath("/generic-user");
		pwd = kdb.getPassword(ep);
		assertTrue(pwd.isPresent());
		assertEquals("password", pwd.get());
	}

	@Test
	void getURL() throws Exception  {
		KeePassDB kdb = getTestKDB();
		EntryPath ep;
		Optional<String> url;

		ep = new EntryPath("/generic-user");
		url = kdb.getURL(ep);
		assertTrue(url.isPresent());
		assertEquals("https://www.example.com", url.get());
	}

	@Test
	void getProperty() throws Exception  {
		KeePassDB kdb = getTestKDB();
		EntryPath ep;
		Optional<String> property;

		ep = new EntryPath("/generic-user");
		property = kdb.getProperty(ep, "field");
		assertTrue(property.isPresent());
		assertEquals("value", property.get());
	}

	@Test
	void getNonExistingProperty() throws Exception  {
		KeePassDB kdb = getTestKDB();
		EntryPath ep;
		Optional<String> property;

		ep = new EntryPath("/generic-user");
		property = kdb.getProperty(ep, "no-field");
		assertFalse(property.isPresent());
	}

	@Test
	void getBinaryProperty() throws Exception  {
		KeePassDB kdb = getTestKDB();
		EntryPath ep;
		Optional<byte[]> bytes;

		ep = new EntryPath("/generic-user");
		bytes = kdb.getBinaryProperty(ep, "text.txt");
		assertTrue(bytes.isPresent());
		assertEquals("Hello KeePass", new String(bytes.get(), Charset.forName("UTF-8")));
	}
}
