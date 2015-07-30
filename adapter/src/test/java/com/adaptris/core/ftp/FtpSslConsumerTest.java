package com.adaptris.core.ftp;


public class FtpSslConsumerTest extends FtpConsumerCase {

  private static final String BASE_DIR_KEY = "FtpsConsumerExamples.baseDir";

  public FtpSslConsumerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected FtpSslConnection createConnectionForExamples() {
    FtpSslConnection con = new FtpSslConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");

    return con;
  }

  @Override
  protected String getScheme() {
    return "ftps";
  }

}
