package com.adaptris.core.ftp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.LenientKnownHosts;
import com.adaptris.sftp.SftpClient;
import com.adaptris.sftp.SftpConnectionBehaviour;
import com.adaptris.sftp.StrictKnownHosts;

public class SftpConnectionUsingKeyTest extends FtpCase {

  // stop jsch auto-update host key

  protected static final String CFG_HOST = "SftpConsumerTest.host";
  protected static final String CFG_USER = "SftpConsumerTest.username";
  protected static final String CFG_PASSWORD = "SftpConsumerTest.password";
  protected static final String CFG_REMOTE_DIR = "SftpConsumerTest.remotedir";

  protected static final String CFG_PRIVATE_KEY_FILE = "SftpConsumerTest.privateKeyFile";
  protected static final String CFG_PRIVATE_KEY_PW = "SftpConsumerTest.privateKeyPassword";

  // DO NOT use the top two directly, use @setupTempHostsFile and the CFG_TEMP_HOSTS_FILE
  protected static final String CFG_KNOWN_HOSTS_FILE = "SftpConsumerTest.knownHostsFile";
  protected static final String CFG_UNKNOWN_HOSTS_FILE = "SftpConsumerTest.unknownHostsFile";
  protected static final String CFG_TEMP_HOSTS_FILE = "SftpConsumerTest.tempHostsFile";

  private FtpConsumer consumer;
  private MockMessageListener listener;

  public SftpConnectionUsingKeyTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    if (areTestsEnabled()) {
      listener = new MockMessageListener();
      ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationString());
      ccd.setConfiguredThreadName("SftpTest");
      consumer = new FtpConsumer();
      consumer.setDestination(ccd);
      consumer.setWorkDirectory(DEFAULT_WORK_DIR);
      consumer.registerAdaptrisMessageListener(listener);
      consumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(CFG_TEMP_HOSTS_FILE)));
  }

  public void testSetSshConnectionBehaviour() throws Exception {
    SftpKeyAuthConnection conn = createConnection();
    assertNotNull(conn.getSftpConnectionBehaviour());
    assertEquals(DefaultSftpBehaviour.class, conn.getSftpConnectionBehaviour().getClass());

    StrictKnownHosts skh = new StrictKnownHosts();

    conn.setSftpConnectionBehaviour(skh);
    assertEquals(skh, conn.getSftpConnectionBehaviour());
    try {
      conn.setSftpConnectionBehaviour(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(skh, conn.getSftpConnectionBehaviour());
  }

  public void testConnectOnly_DefaultBehaviour() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      conn.setSftpConnectionBehaviour(new DefaultSftpBehaviour());
      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        assertTrue(c.isConnected());
        c.disconnect();
      }
      finally {
        stop(conn);
      }
    }
  }

  public void testConnectOnly_Compression() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      StrictKnownHosts knownHosts = new StrictKnownHosts(tempHostsFile.getCanonicalPath(), true);

      conn.setSftpConnectionBehaviour(knownHosts);
      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        assertTrue(c.isConnected());
        c.disconnect();
      }
      finally {
        stop(conn);
      }
    }

  }

  public void testConnectOnly_StrictKnownHosts_UnknownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      StrictKnownHosts knownHosts = new StrictKnownHosts(tempHostsFile.getCanonicalPath(), false);

      conn.setSftpConnectionBehaviour(knownHosts);
      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        fail();
      }
      catch (FileTransferException expected) {

      }
      finally {
        stop(conn);
      }
    }
  }

  public void testConnectOnly_StrictKnownHosts_KnownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      StrictKnownHosts knownHosts = new StrictKnownHosts(tempHostsFile.getCanonicalPath(), false);

      conn.setSftpConnectionBehaviour(knownHosts);
      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        assertTrue(c.isConnected());
        c.disconnect();
      }
      finally {
        stop(conn);
      }
    }
  }

  public void testConnectOnly_LenientKnownHosts_UnknownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      LenientKnownHosts knownHosts = new LenientKnownHosts(tempHostsFile.getCanonicalPath(), false);

      conn.setSftpConnectionBehaviour(knownHosts);
      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        assertTrue(c.isConnected());
        c.disconnect();
      }
      finally {
        stop(conn);
      }
    }
  }

  public void testConnectOnly_LenientKnownHosts_KnownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      LenientKnownHosts knownHosts = new LenientKnownHosts(tempHostsFile.getCanonicalPath(), false);

      conn.setSftpConnectionBehaviour(knownHosts);
      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        assertTrue(c.isConnected());
        c.disconnect();
      }
      finally {
        stop(conn);
      }
    }
  }

  public void testConnect_InvalidPrivateKeyPassword() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      StrictKnownHosts knownHosts = new StrictKnownHosts(tempHostsFile.getCanonicalPath(), false);

      conn.setSftpConnectionBehaviour(knownHosts);
      conn.setPrivateKeyPassword("PW:ABCDEF012345");

      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        fail();
      }
      catch (FileTransferException expected) {
      }
      finally {
        stop(conn);
      }
    }
  }

  public void testConnect_InvalidPrivateKeyFile() throws Exception {

    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      StrictKnownHosts knownHosts = new StrictKnownHosts(tempHostsFile.getCanonicalPath(), false);

      conn.setSftpConnectionBehaviour(knownHosts);
      conn.setPrivateKeyFilename("/some/file/that/does/not/exist");

      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        fail();
      }
      catch (IOException expected) {

      }
      finally {
        stop(conn);
      }
    }
  }

  public void testConsume_StrictKnownHosts_Known() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpKeyAuthConnection conn = createConnection();
      StrictKnownHosts skh = new StrictKnownHosts();
      skh.setKnownHostsFile(tempHostsFile.getCanonicalPath());
      conn.setSftpConnectionBehaviour(skh);

      StandaloneConsumer sc = new StandaloneConsumer(conn, consumer);
      sc.init();
      sc.start();
      int count = 1;
      try {
        produce(new StandaloneProducer(conn, createFtpProducer()), count);
        waitForMessages(listener, count);
        assertMessages(listener.getMessages(), count);
      }
      finally {
        sc.stop();
        sc.close();
      }
    }
  }

  public void testConsume_LenientKnownHosts_Unknown() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

      // Step 1, this should add the host to the tempfile
      SftpKeyAuthConnection conn = createConnection();
      LenientKnownHosts khh = new LenientKnownHosts();
      khh.setKnownHostsFile(tempHostsFile.getCanonicalPath());
      conn.setSftpConnectionBehaviour(khh);

      StandaloneConsumer sc = new StandaloneConsumer(conn, consumer);
      sc.init();
      sc.start();
      int count = 1;
      try {
        produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
        while (listener.getMessages().size() < count) {
          try {
            Thread.sleep(100);
          }
          catch (Exception e) {
          }
        }
        assertMessages(listener.getMessages(), count);

      }
      finally {
        sc.stop();
        sc.close();
      }
    }
  }

  private File copyHostsFile(File srcKnownHosts) throws Exception {
    File tempDir = new File(PROPERTIES.getProperty(CFG_TEMP_HOSTS_FILE));
    tempDir.mkdirs();
    File tempFile = File.createTempFile(SftpConnectionUsingKeyTest.class.getSimpleName(), "", tempDir);
    FileUtils.copyFile(srcKnownHosts, tempFile);
    return tempFile;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  private StandaloneConsumer createConsumerExample(SftpConnectionBehaviour behavior, Poller poller) {
    SftpKeyAuthConnection con = new SftpKeyAuthConnection();
    FtpConsumer cfgConsumer = new FtpConsumer();
    try {
      con.setPrivateKeyFilename("/path/to/private/key");
      con.setPrivateKeyPassword(Password.encode("MyPassword", Password.PORTABLE_PASSWORD));
      con.setSftpConnectionBehaviour(behavior);
      con.setDefaultUserName("UserName if Not configured in destination");
      cfgConsumer.setProcDirectory("/proc");
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
    SftpKeyAuthConnection con = (SftpKeyAuthConnection) ((StandaloneConsumer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getSftpConnectionBehaviour().getClass().getSimpleName()
        + "-SFTP-KeyBasedAuth";
  }

  @Override
  protected String getDestinationString() {
    return "sftp://" + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected String getDestinationStringWithOverride() throws Exception {
    return "sftp://" + PROPERTIES.getProperty(CFG_USER) + ":" + Password.decode(PROPERTIES.getProperty(CFG_PASSWORD)) + "@"
        + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected FileTransferClient connectRawClient() throws Exception {
    SftpClient client = new SftpClient(PROPERTIES.getProperty(CFG_HOST));
    client.connect(PROPERTIES.getProperty(CFG_USER), Password.decode(PROPERTIES.getProperty(CFG_PASSWORD)));
    return client;
  }

  @Override
  protected String getRemoteDirectory() {
    return PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected SftpKeyAuthConnection createConnection() throws Exception {
    SftpKeyAuthConnection c = new SftpKeyAuthConnection();
    c.setPrivateKeyFilename(PROPERTIES.getProperty(CFG_PRIVATE_KEY_FILE));
    c.setPrivateKeyPassword(PROPERTIES.getProperty(CFG_PRIVATE_KEY_PW));
    c.setDefaultUserName(PROPERTIES.getProperty(CFG_USER));
    c.setAdditionalDebug(true);
    return c;
  }

}
