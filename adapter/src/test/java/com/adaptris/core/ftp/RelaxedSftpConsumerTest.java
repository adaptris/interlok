package com.adaptris.core.ftp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.LenientKnownHosts;
import com.adaptris.sftp.SftpConnectionBehaviour;
import com.adaptris.sftp.StrictKnownHosts;


public class RelaxedSftpConsumerTest extends RelaxedFtpConsumerCase {

  private static final String BASE_DIR_KEY = "SftpConsumerExamples.baseDir";

  public RelaxedSftpConsumerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
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

  @Override
  protected String getScheme() {
    return "sftp";
  }

  private StandaloneConsumer createConsumerExample(SftpConnectionBehaviour behavior, Poller poller) {
    SftpConnection con = createConnectionForExamples();
    RelaxedFtpConsumer cfgConsumer = new RelaxedFtpConsumer();
    try {
      con.setSftpConnectionBehaviour(behavior);
      con.setDefaultUserName("UserName if Not configured in destination");
      cfgConsumer.setDestination(new ConfiguredConsumeDestination("sftp://overrideuser@hostname:port/path/to/directory", "*.xml"));
      cfgConsumer.setPoller(poller);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new StandaloneConsumer(con, cfgConsumer);
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return new ArrayList(Arrays.asList(new StandaloneConsumer[]
    {
        createConsumerExample(new LenientKnownHosts("/path/to/known/hosts", false), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(new LenientKnownHosts("/path/to/known/hosts", false), new FixedIntervalPoller()),
        createConsumerExample(new StrictKnownHosts("/path/to/known/hosts", false), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(new StrictKnownHosts("/path/to/known/hosts", false), new FixedIntervalPoller()),
        createConsumerExample(new DefaultSftpBehaviour(), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(new DefaultSftpBehaviour(), new FixedIntervalPoller()),
    }));
  }

  @Override
  protected String createBaseFileName(Object object) {
    SftpConnection con = (SftpConnection) ((StandaloneConsumer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getClass().getSimpleName() + "-"
        + con.getSftpConnectionBehaviour().getClass().getSimpleName();
  }
}
