package com.adaptris.core.management;

import static com.adaptris.core.management.Constants.SYSTEM_PROPERTY_PREFIX;
import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.security.password.Password;

public class SystemPropertiesUtilTest {

  private static final String DEFAULT_VALUE = "Back At The Chicken Shack 1960";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAddSystemProperties() throws Exception {
    Properties p = new Properties();
    p.setProperty(SYSTEM_PROPERTY_PREFIX + "zzlc.plain", DEFAULT_VALUE);
    SystemPropertiesUtil.addSystemProperties(p);
    assertEquals(DEFAULT_VALUE, System.getProperty("zzlc.plain"));
  }


  @Test
  public void testAddEncodingSystemProperty() throws Exception {
    Properties p = new Properties();
    p.setProperty(SYSTEM_PROPERTY_PREFIX + "zzlc.encrypted", encode(DEFAULT_VALUE));
    SystemPropertiesUtil.addSystemProperties(p);
    assertEquals(DEFAULT_VALUE, System.getProperty("zzlc.encrypted"));
  }

  public static String encode(String thePassword) throws Exception {
    return "{password}" + Password.encode(thePassword, Password.PORTABLE_PASSWORD);
  }
}
