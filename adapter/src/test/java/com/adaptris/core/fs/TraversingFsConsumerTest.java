package com.adaptris.core.fs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

public class TraversingFsConsumerTest extends FsConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String EXAMPLE_BASEDIR = "TraversingFsConsumerExample.baseDir";

  public TraversingFsConsumerTest(java.lang.String testName) {
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
    FsConsumer fs = createConsumer(subDir);
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      start(sc);
      createFiles(baseDir, ".xml", count);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count);
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  public void testConsume_WithFilter() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.getDestination().setFilterExpression(".*xml");
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      start(sc);
      createFiles(baseDir, ".xml", count);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count);
    }
    finally {
      stop(sc);
      File baseDir = new File(parentDir, subDir);
    }
  }

  protected void assertMessages(List<AdaptrisMessage> list, int count) {
    assertEquals("All files produced", count, list.size());
    for (AdaptrisMessage m : list) {
      assertEquals(0, m.getSize());
      assertTrue(m.containsKey(CoreConstants.ORIGINAL_NAME_KEY));
      assertTrue(m.containsKey(CoreConstants.FILE_LAST_MODIFIED_KEY));
      assertTrue(m.containsKey(CoreConstants.FS_CONSUME_DIRECTORY));
    }

  }

  @Override
  protected TraversingFsConsumer createConsumer(String subDir) {
    String destinationName = subDir == null ? PROPERTIES.getProperty(BASE_KEY) : PROPERTIES.getProperty(BASE_KEY) + "/" + subDir;
    TraversingFsConsumer fs = new TraversingFsConsumer();
    fs.setDestination(new ConfiguredConsumeDestination(destinationName));
    fs.setCreateDirs(true);
    return fs;
  }

  private List<File> createFiles(File baseDir, String ext, int count) throws IOException {
    List<File> result = new ArrayList<File>();
    baseDir.mkdirs();
    for (int i = 0; i < count; i++) {
      File subDir = createDirectoryTree(baseDir);
      if (!subDir.exists()) {
        subDir.mkdirs();
      }
      result.add(File.createTempFile("FSC", ext, subDir));
    }
    return result;
  }

  private File createDirectoryTree(File baseDir) {
    File result = baseDir;
    int i = new Random().nextInt(20);
    switch (i % 3) {
    case 0:
      result = createDirectoryTree(new File(baseDir, new GuidGenerator().safeUUID()));
      break;
    case 1:
      result = new File(baseDir, new GuidGenerator().safeUUID());
      break;
    default:
      result = baseDir;
      break;
    }
    return result;
  }

  @Override
  protected TraversingFsConsumer createConsumer() {
    return new TraversingFsConsumer();
  }

  @Override
  protected void assertMessages(List<AdaptrisMessage> list, int count, File[] remaining) {
    assertMessages(list, count);
  }
}
