package com.adaptris.core.ftp;

public class FtpSslConnectionTest extends FtpConnectionTest {

  public FtpSslConnectionTest(String name) {
    super(name);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected FtpSslConnection createConnectionObj() {
    return new FtpSslConnection();
  }

  @Override
  protected String getScheme() {
    return "ftps";
  }

}
