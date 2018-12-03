package com.adaptris.sftp;

public class TestSocks4Proxy extends ViaProxyCase {

  @Override
  protected ViaSocks4Proxy createBuilder() {
    return new ViaSocks4Proxy();
  }

}
