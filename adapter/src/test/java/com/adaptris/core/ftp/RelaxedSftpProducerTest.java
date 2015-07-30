package com.adaptris.core.ftp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.LenientKnownHosts;
import com.adaptris.sftp.SftpConnectionBehaviour;
import com.adaptris.sftp.StrictKnownHosts;

public class RelaxedSftpProducerTest extends RelaxedFtpProducerCase {

  private static final String BASE_DIR_KEY = "SftpProducerExamples.baseDir";

  public RelaxedSftpProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }


  @Override
  protected SftpConnection createConnectionForExamples() {
    SftpConnection con = new SftpConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");
    con.setSftpConnectionBehaviour(new LenientKnownHosts());
    return con;
  }

  private StandaloneProducer createProducerExample(SftpConnectionBehaviour behaviour) {
    SftpConnection con = createConnectionForExamples();
    RelaxedFtpProducer producer = createProducerExample();
    try {
      con.setSftpConnectionBehaviour(behaviour);
      producer.setFileNameCreator(new FormattedFilenameCreator());
      producer.setDestination(new ConfiguredProduceDestination("sftp://sftpuser@hostname:port/path/to/directory"));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new StandaloneProducer(con, producer);
  }

  @Override
  protected String createBaseFileName(Object object) {
    SftpConnection con = (SftpConnection) ((StandaloneProducer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getClass().getSimpleName() + "-"
        + con.getSftpConnectionBehaviour().getClass().getSimpleName();
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return new ArrayList(Arrays.asList(new StandaloneProducer[]
    {
        createProducerExample(new LenientKnownHosts("/path/to/known/hosts", false)),
        createProducerExample(new StrictKnownHosts("/path/to/known/hosts", false)),
        createProducerExample(new DefaultSftpBehaviour())
    }));
  }

  @Override
  protected String getScheme() {
    return "sftp";
  }

}
