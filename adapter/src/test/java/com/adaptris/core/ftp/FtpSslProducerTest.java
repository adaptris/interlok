package com.adaptris.core.ftp;


public class FtpSslProducerTest extends FtpProducerCase {

  private static final String BASE_DIR_KEY = "FtpsProducerExamples.baseDir";

  public FtpSslProducerTest(String name) {
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
