package com.adaptris.sftp;

public class TestSocks5Proxy extends ViaProxyCase {

  @Override
  protected ViaSocks5Proxy createBuilder() {
    return new ViaSocks5Proxy();
  }

}
