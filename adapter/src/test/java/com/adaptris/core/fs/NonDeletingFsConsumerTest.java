package com.adaptris.core.fs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.oro.io.Perl5FilenameFilter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

public class NonDeletingFsConsumerTest extends FsConsumerCase {
  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String EXAMPLE_BASEDIR = "NonDeletingFsConsumerExample.baseDir";

  public NonDeletingFsConsumerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected void configureExampleConfigBaseDir() {
    if (PROPERTIES.getProperty(EXAMPLE_BASEDIR) != null) {
      setBaseDir(PROPERTIES.getProperty(EXAMPLE_BASEDIR));
    }
  }

  public void testConsume() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDir, "testConsume");
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    catch (Exception e) {
      log.warn(e.getMessage(), e);
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  public void testBug2100_OriginalNameContainsWip() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDir, "testBug2100_OriginalNameContainsWip");
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
      for (AdaptrisMessage msg : stub.getMessages()) {
        assertFalse("original name should not contain '.wip'",
            msg.getMetadataValue(CoreConstants.ORIGINAL_NAME_KEY).endsWith(".wip"));
      }
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  public void testConsumeNotReprocessed() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDir, "testConsume");
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      Thread.sleep(2000); // This should re-trigger the poll, so let's make sure
      // that we don't reprocess them.
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    catch (Exception e) {
      log.warn(e.getMessage(), e);
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  public void testConsumeWithQuietPeriod() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDir, "testConsumeWithQuietPeriod");
    fs.setQuietInterval(new TimeInterval(1L, TimeUnit.SECONDS));
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));

    }
  }

  public void testBug1675ConsumeWithSpacesInDir() throws Exception {
    String uniqueName = new GuidGenerator().safeUUID();
    String subDir = uniqueName + "Directory WithSpaces";
    String subDirEncoded = uniqueName + "Directory%20WithSpaces";
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDirEncoded, "testBug1675ConsumeWithSpacesInDir");
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    catch (Exception e) {
      log.debug(e.getMessage(), e);
      throw e;
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));

    }
  }

  public void testConsumeWithFilter() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDir, "testConsumeWithFilter");
    ((ConfiguredConsumeDestination) fs.getDestination()).setFilterExpression(".*\\.xml");
    fs.setFileFilterImp(Perl5FilenameFilter.class.getName());
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      createFiles(baseDir, ".tmp", count);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);

      Perl5FilenameFilter wip = new Perl5FilenameFilter(".*\\.tmp");
      assertEquals("TMP Files remain", count, baseDir.listFiles((FilenameFilter) wip).length);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));

    }
  }

  public void testReprocessOnLastModifiedChange() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDir, "testConsume");
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      List<File> createdFiles = createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      Thread.sleep(2000);
      touch(createdFiles);
      waitForMessages(stub, count * 2);
    }
    catch (Exception e) {
      log.warn(e.getMessage(), e);
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  // Technicaly somewhat of a misnomer, you can't change the filesize unless you
  // change lastModifiexd, but hey ho
  public void testReprocessOnFilesizeChange() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    NonDeletingFsConsumer fs = createConsumer(subDir, "testConsume");
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      List<File> createdFiles = createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      Thread.sleep(2000);
      resize(createdFiles);
      waitForMessages(stub, count * 2);
    }
    catch (Exception e) {
      log.warn(e.getMessage(), e);
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  public void testRedmine481_SubDirInConsumeDirectory() throws Exception {
    String consumeDir = new GuidGenerator().safeUUID();
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    File subDirectory =
        FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY) + "/" + consumeDir + "/"
            + new GuidGenerator().safeUUID(), true));
    subDirectory.mkdirs();
    NonDeletingFsConsumer fs = createConsumer(consumeDir, "testRedmine481_SubDirInConsumeDirectory");
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    MockMessageListener stub = new MockMessageListener(0);
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    try {
      start(sc);
      Thread.sleep(2000);
      assertEquals(true, subDirectory.exists());
      assertEquals(true, subDirectory.isDirectory());
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, consumeDir));
    }
  }

  @Override
  protected void assertMessages(List<AdaptrisMessage> list, int count, File[] remaining) {
    assertEquals("All files produced", count, list.size());
    assertEquals("All files left in dir", count, remaining.length);
    for (AdaptrisMessage m : list) {
      assertTrue(m.containsKey(CoreConstants.ORIGINAL_NAME_KEY));
      assertTrue(m.containsKey(CoreConstants.FILE_LAST_MODIFIED_KEY));
    }

  }

  protected NonDeletingFsConsumer createConsumer(String subDir, String threadname) {
    String destinationName = subDir == null ? PROPERTIES.getProperty(BASE_KEY) : PROPERTIES.getProperty(BASE_KEY) + "/" + subDir;
    ConfiguredConsumeDestination dest = threadname != null
        ? new ConfiguredConsumeDestination(destinationName, null, threadname)
        : new ConfiguredConsumeDestination(destinationName);

    NonDeletingFsConsumer fs = new NonDeletingFsConsumer(dest);
    fs.setReacquireLockBetweenMessages(true);
    fs.setCreateDirs(true);
    return fs;
  }

  @Override
  protected NonDeletingFsConsumer createConsumer(String subDir) {
    return createConsumer(subDir, null);
  }

  @Override
  protected NonDeletingFsConsumer createConsumer() {
    return new NonDeletingFsConsumer();
  }

  private void touch(List<File> files) throws IOException {
    for (File f : files) {
      f.setLastModified(System.currentTimeMillis());
    }
  }

  private void resize(List<File> files) throws IOException {
    for (File f : files) {
      long currentSize = f.length();
      RandomAccessFile raf = new RandomAccessFile(f, "rwd");
      try {
        raf.setLength(currentSize + 1);
      }
      finally {
        raf.close();
      }
    }
  }

  private List<File> createFiles(File baseDir, String ext, int count) throws IOException {
    baseDir.mkdirs();
    List<File> result = new ArrayList<File>();
    for (int i = 0; i < count; i++) {
      result.add(File.createTempFile("FSC", ext, baseDir));
    }
    return result;
  }
}
