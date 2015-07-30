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

public class SftpProducerTest extends FtpProducerCase {

  private static final String BASE_DIR_KEY = "SftpProducerExamples.baseDir";

  public SftpProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  private StandaloneProducer createProducerExample(SftpConnectionBehaviour behaviour) {
    SftpConnection con = createConnectionForExamples();
    FtpProducer producer = createProducerExample();
    try {
      con.setSftpConnectionBehaviour(behaviour);
      producer.setFilenameCreator(new FormattedFilenameCreator());
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
  protected SftpConnection createConnectionForExamples() {
    SftpConnection con = new SftpConnection();
    con.setDefaultPassword("The Default Password if not provided as part of the destination");
    con.setDefaultUserName("UserName if not configured in destination");
    return con;
  }

  @Override
  protected String getScheme() {
    return "sftp";
  }

}
