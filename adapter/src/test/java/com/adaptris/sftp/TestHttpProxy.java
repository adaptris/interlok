package com.adaptris.sftp;

public class TestHttpProxy extends ViaProxyCase {

  @Override
  protected ViaHttpProxy createBuilder() {
    return new ViaHttpProxy();
  }

}
