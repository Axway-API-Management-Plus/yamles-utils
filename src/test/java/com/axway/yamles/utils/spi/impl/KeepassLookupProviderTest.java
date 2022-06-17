package com.axway.yamles.utils.spi.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.KeePassDBTest;
import com.axway.yamles.utils.spi.impl.KeepassLookupProvider.Kdb;
import com.axway.yamles.utils.spi.impl.KeepassLookupProvider.Key;
import com.axway.yamles.utils.spi.impl.KeepassLookupProvider.What;

class KeepassLookupProviderTest {

	@Test
	void parseInvalidKey() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse(null);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse(":");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:foo");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:user.");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:user.field");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:password.");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:password.field");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:prop");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:prop.");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:binUTF8.");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Key.parse("/path:binB64.");
		});
	}
	
	@Test
	void parseKey() {
		Key k;
		
		k = Key.parse("/path:user");
		assertEquals("/path", k.ep.toString());
		assertSame(What.user, k.what);
		assertNull(k.pname);
		
		k = Key.parse("/path:password");
		assertEquals("/path", k.ep.toString());
		assertSame(What.password, k.what);
		assertNull(k.pname);

		k = Key.parse("/path:prop.field");
		assertEquals("/path", k.ep.toString());
		assertSame(What.prop, k.what);
		assertEquals("field", k.pname);

		k = Key.parse("/path:binB64.field");
		assertEquals("/path", k.ep.toString());
		assertSame(What.binB64, k.what);
		assertEquals("field", k.pname);

		k = Key.parse("/path:binUTF8.field");
		assertEquals("/path", k.ep.toString());
		assertSame(What.binUTF8, k.what);
		assertEquals("field", k.pname);
		
		k = Key.parse("/path:binISO8859.field");
		assertEquals("/path", k.ep.toString());
		assertSame(What.binISO8859, k.what);
		assertEquals("field", k.pname);
		
		k = Key.parse("/path:/entry::binB64.field.field");
		assertEquals("/path:/entry:", k.ep.toString());
		assertSame(What.binB64, k.what);
		assertEquals("field.field", k.pname);
	}
	
	@Test
	void getValues() throws Exception {
		Kdb kdb = new Kdb();
		kdb.db = KeePassDBTest.getTestKDB();
		
		String expectedBin = "Hello KeePass";
		String expectedB64 = Base64.getEncoder().encodeToString("Hello KeePass".getBytes());
		
		assertEquals("user", kdb.getValue("/generic-user:user").get());
		assertEquals("password", kdb.getValue("/generic-user:password").get());
		assertEquals("value", kdb.getValue("/generic-user:prop.field").get());
		assertEquals(expectedBin, kdb.getValue("/generic-user:binUTF8.text.txt").get());
		assertEquals(expectedBin, kdb.getValue("/generic-user:binISO8859.text.txt").get());
		assertEquals(expectedBin, kdb.getValue("/generic-user:binISO8859.text.txt").get());
		assertEquals(expectedB64, kdb.getValue("/generic-user:binB64.text.txt").get());
	}
}
