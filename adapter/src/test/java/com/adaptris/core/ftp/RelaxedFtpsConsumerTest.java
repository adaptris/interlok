package com.adaptris.core.ftp;



public class RelaxedFtpsConsumerTest extends RelaxedFtpConsumerCase {

  private static final String BASE_DIR_KEY = "FtpsConsumerExamples.baseDir";

  public RelaxedFtpsConsumerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected FtpConnection createConnectionForExamples() {
    FtpConnection con = new FtpConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");

    con.setAdditionalDebug(false);
    return con;
  }

  @Override
  protected String getScheme() {
    return "ftps";
  }
}
