package com.axway.yamles.utils.spi.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.KeePassDB.EntryPath;
import com.axway.yamles.utils.helper.KeePassDBTest;
import com.axway.yamles.utils.spi.impl.KeepassLookupProvider.Kdb;
import com.axway.yamles.utils.spi.impl.KeepassLookupProvider.Key;
import com.axway.yamles.utils.spi.impl.KeepassLookupProvider.What;

class KeepassLookupProviderTest {

	private static Key key(String path, What what) {
		return key(path, what, null);
	}

	private static Key key(String path, What what, String pname) {
		return new Key(new EntryPath(path), what, pname);
	}

	@Test
	void getValues() throws Exception {
		Kdb kdb = new Kdb("test", KeePassDBTest.getTestKDB());

		String expectedBin = "Hello KeePass";
		String expectedB64 = Base64.getEncoder().encodeToString("Hello KeePass".getBytes());

		assertEquals("user", kdb.getValue(key("/generic-user", What.user)).get());
		assertEquals("password", kdb.getValue(key("/generic-user", What.password)).get());
		assertEquals("https://www.example.com", kdb.getValue(key("/generic-user", What.url)).get());
		assertEquals("value", kdb.getValue(key("/generic-user", What.prop, "field")).get());
		assertEquals(expectedBin, kdb.getValue(key("/generic-user", What.binUTF8, "text.txt")).get());
		assertEquals(expectedBin, kdb.getValue(key("/generic-user", What.binISO8859, "text.txt")).get());
		assertEquals(expectedB64, kdb.getValue(key("/generic-user", What.binB64, "text.txt")).get());
	}
}
