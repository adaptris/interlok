package com.adaptris.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.adaptris.security.password.Password;

public class ConfiguredPrivateKeyPasswordProviderTest {

  @Test
  public void testGetPassword() throws Exception {
    ConfiguredPrivateKeyPasswordProvider pkpp = new ConfiguredPrivateKeyPasswordProvider("ABCDE");
    assertEquals("ABCDE", new String(pkpp.retrievePrivateKeyPassword()));
    pkpp = new ConfiguredPrivateKeyPasswordProvider(Password.encode("ABCDE", Password.PORTABLE_PASSWORD));
    assertEquals("ABCDE", new String(pkpp.retrievePrivateKeyPassword()));
  }

  @Test
  public void testGetNullPassword() throws Exception {
    ConfiguredPrivateKeyPasswordProvider pkpp = new ConfiguredPrivateKeyPasswordProvider(null);
    assertNull(pkpp.retrievePrivateKeyPassword());
  }

  @Test
  public void testGetEmpty() throws Exception {
    ConfiguredPrivateKeyPasswordProvider pkpp = new ConfiguredPrivateKeyPasswordProvider("");
    assertNull(pkpp.retrievePrivateKeyPassword());
  }
}
