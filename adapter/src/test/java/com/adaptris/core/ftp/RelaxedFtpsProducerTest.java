package com.adaptris.core.ftp;



public class RelaxedFtpsProducerTest extends RelaxedFtpProducerCase {

  private static final String BASE_DIR_KEY = "FtpsProducerExamples.baseDir";

  public RelaxedFtpsProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected void setUp() throws Exception {
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
    return "ftp";
  }

}
