package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.http.auth.AdapterResourceAuthenticator;

public class ResourceAuthenticatorTest {

  private static final String TARGET = "http://localhost";
  
  private transient DummyResourceAuthenticator dummyAuth =
      new DummyResourceAuthenticator(new PasswordAuthentication("user", "password".toCharArray()));

  @Before
  public void setup() {
    AdapterResourceAuthenticator.getInstance().addAuthenticator(dummyAuth);
    Authenticator.setDefault(AdapterResourceAuthenticator.getInstance());
  }

  @After
  public void teardown() {
    AdapterResourceAuthenticator.getInstance().removeAuthenticator(dummyAuth);
    Authenticator.setDefault(null);
  }

  @Test
  public void testRequestAuthentication() throws Exception {
    PasswordAuthentication auth = Authenticator.requestPasswordAuthentication("localhost", InetAddress.getLocalHost(), 80, "http",
        "", "http", new URL(TARGET), RequestorType.SERVER);
    assertNotNull(auth);
    assertEquals("user", auth.getUserName());
    assertEquals("password", String.valueOf(auth.getPassword()));
  }


  private class DummyResourceAuthenticator implements ResourceAuthenticator {

    private PasswordAuthentication myPasswordAuth;

    public DummyResourceAuthenticator(PasswordAuthentication auth) {
      myPasswordAuth = auth;
    }

    @Override
    public PasswordAuthentication authenticate(ResourceTarget target) {
      try {
        assertNotNull(target.toString());
        assertTrue(target.toString().contains("localhost"));
        assertTrue(target.toString().contains("http"));
        assertEquals("localhost", target.getRequestingHost());
        assertEquals(80, target.getRequestingPort());
        assertEquals("http", target.getRequestingProtocol());
        assertEquals("http", target.getRequestingScheme());
        assertEquals("", target.getRequestingPrompt());
        assertEquals(InetAddress.getLocalHost(), target.getRequestingSite());
        assertEquals(new URL(TARGET), target.getRequestingURL());
        assertEquals(RequestorType.SERVER, target.getRequestorType());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return myPasswordAuth;
    }
    
  }
}
