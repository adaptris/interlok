package com.adaptris.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import com.adaptris.security.password.Password;

public class LegacyPrivateKeyPasswordProviderTest {

  @Test
  public void testGetPassword() throws Exception {
    LegacyPrivateKeyPasswordProvider pkpp = new LegacyPrivateKeyPasswordProvider();
    assertEquals(getPassword(), new String(pkpp.retrievePrivateKeyPassword()));
    // Do it twice to do all coverage... lame but hey.
    assertEquals(getPassword(), new String(pkpp.retrievePrivateKeyPassword()));
  }

  private String getPassword() throws Exception {
    String result = null;
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("security.properties");
    if (is != null) {
      Properties p = new Properties();
      p.load(is);
      result = Password.decode(p.getProperty("adaptris.privatekey.password"));
    }
    else {
      fail("Couldn't find security.properties");
    }
    return result;
  }
}
