package com.adaptris.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public abstract class ConfigBuilderCase {

  @Test
  public void testProxy() throws Exception {
    ConfigBuilderImpl pb = createBuilder();
    assertNull(pb.getProxy());
    assertNull(pb.buildProxy());
    pb.setProxy(new ViaHttpProxy("localhost:80"));
    assertEquals(ViaHttpProxy.class, pb.getProxy().getClass());
    assertNotNull(pb.buildProxy());
  }

  @Test
  public void testConfigRepository() throws Exception {
    ConfigBuilderImpl pb = createBuilder();
    assertNotNull(pb.buildConfigRepository());
  }

  protected abstract ConfigBuilderImpl createBuilder() throws Exception;
}
