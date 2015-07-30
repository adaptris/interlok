package com.adaptris.core.ftp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.LenientKnownHosts;
import com.adaptris.sftp.SftpConnectionBehaviour;
import com.adaptris.sftp.StrictKnownHosts;

public class SftpProducerWithKeyTest extends FtpProducerExample {

  public SftpProducerWithKeyTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  private StandaloneProducer createProducerExample(SftpConnectionBehaviour behaviour) {
    SftpKeyAuthConnection con = new SftpKeyAuthConnection();
    FtpProducer producer = new FtpProducer();
    try {
      con.setPrivateKeyFilename("/path/to/private/key");
      con.setPrivateKeyPassword(Password.encode("MyPassword", Password.PORTABLE_PASSWORD));
      con.setSftpConnectionBehaviour(behaviour);
      con.setDefaultUserName("UserName if Not configured in destination");
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
    SftpKeyAuthConnection con = (SftpKeyAuthConnection) ((StandaloneProducer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getSftpConnectionBehaviour().getClass().getSimpleName()
        + "-SFTP-KeyBasedAuth";
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

}
