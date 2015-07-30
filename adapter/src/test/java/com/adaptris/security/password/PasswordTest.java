package com.adaptris.security.password;

import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.system.Os;

public class PasswordTest {

  private static final String TEXT = "MYPASSWORD";

  private static Log logR = LogFactory.getLog(PasswordTest.class);

  @Before
  public void setUp() throws Exception {
    logR = LogFactory.getLog(this.getClass());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testMainClass() throws Exception {
    String[] args =
    {
        Password.PORTABLE_PASSWORD, TEXT
    };
    Password.main(args);
    Password.main(new String[0]);
  }

  @Test
  public void testPortable() throws Exception {
    String encoded = Password.encode(TEXT, Password.PORTABLE_PASSWORD);
    assertEquals(TEXT, Password.decode(encoded));
  }

  @Test
  public void testNonPortable() throws Exception {
    String encoded = Password.encode(TEXT, Password.NON_PORTABLE_PASSWORD);
    assertEquals(TEXT, Password.decode(encoded));
  }

  @Test
  public void testMicrosoftCrypto() throws Exception {
    if (Os.isFamily(Os.WINDOWS_NT_FAMILY)) {
      String encoded = Password.encode(TEXT, Password.MSCAPI_STYLE);
      assertEquals(TEXT, Password.decode(encoded));
    }
  }
}
