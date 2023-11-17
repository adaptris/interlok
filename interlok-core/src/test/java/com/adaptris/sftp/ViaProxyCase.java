package com.adaptris.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.jcraft.jsch.Proxy;

public abstract class ViaProxyCase {
  
  

  @Test
  public void testSetProxy() throws Exception {
    ViaProxy pb = createBuilder();
    assertNull(pb.getProxy());
    pb.setProxy("host:80");
    assertEquals("host:80", pb.getProxy());
    assertNotNull(pb.buildProxy());
  }

  @Test
  public void testSetUsername() throws Exception {
    ViaProxy pb = createBuilder();
    assertNull(pb.getUsername());
    pb.setUsername("username");
    assertEquals("username", pb.getUsername());
    assertNull(pb.buildProxy());
  }

  @Test
  public void testSetPassword() throws Exception {
    ViaProxy pb = createBuilder();
    assertNull(pb.getPassword());
    pb.setPassword("password");
    assertEquals("password", pb.getPassword());
    assertNull(pb.buildProxy());
  }

  @Test
  public void testCreateProxy() throws Exception {
    ViaProxy pb = createBuilder();
    pb.setProxy("host:80");
    assertNotNull(pb.buildProxy());
  }

  @Test
  public void testCreateProxy_NoPort() throws Exception {
    ViaProxy pb = createBuilder();
    pb.setProxy("host");
    assertNotNull(pb.buildProxy());
  }

  @Test
  public void testCreateProxy_NoHost() throws Exception {
    ViaProxy pb = createBuilder();
    pb.setProxy(":");
    assertNull(pb.buildProxy());
    pb.setProxy("");
    assertNull(pb.buildProxy());
    pb.setProxy(null);
    assertNull(pb.buildProxy());
  }

  @Test
  public void testCreateProxy_WithCredentials(TestInfo info) throws Exception {
    ViaProxy pb = createBuilder();
    pb.setProxy("host:80");
    pb.setUsername(info.getDisplayName());
    pb.setPassword(info.getDisplayName());
    Proxy proxy = pb.buildProxy();
    assertNotNull(proxy);
    assertValue(proxy, "user", info.getDisplayName());
    assertValue(proxy, "passwd", info.getDisplayName());
  }

  @Test
  public void testCreateProxy_WithCredentials_BadPassword(TestInfo info) throws Exception {
    ViaProxy pb = createBuilder();
    pb.setProxy("host:80");
    pb.setUsername(info.getDisplayName());
    pb.setPassword("PW:" + info.getDisplayName());
    try {
      Proxy proxy = pb.buildProxy();
      fail();
    } catch (SftpException expected) {

    }
  }

  @Test
  public void testCreateProxy_PasswordNoUser(TestInfo info) throws Exception {
    ViaProxy pb = createBuilder();
    pb.setProxy("host:80");
    pb.setPassword(info.getDisplayName());
    Proxy proxy = pb.buildProxy();
    assertNotNull(proxy);
    assertValue(proxy, "user", null);
    assertValue(proxy, "passwd", null);
  }

  protected void assertValue(Proxy proxy, String field, String value) throws Exception {
    Field f = proxy.getClass().getDeclaredField(field);
    f.setAccessible(true);
    String fieldValue = (String) f.get(proxy);
    if (value != null) {
      assertEquals(value, fieldValue);
    } else {
      assertNull(fieldValue);
    }
  }

  protected abstract ViaProxy createBuilder();
}
