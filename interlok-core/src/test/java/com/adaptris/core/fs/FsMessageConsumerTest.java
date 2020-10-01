/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FileUtils;
import org.apache.oro.io.Perl5FilenameFilter;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.fs.enhanced.AlphabeticAscending;
import com.adaptris.core.fs.enhanced.LastModifiedAscending;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class FsMessageConsumerTest extends FsConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String EXAMPLE_BASEDIR = "FsConsumerExample.baseDir";


  @Override
  protected void configureExampleConfigBaseDir() {
    if (PROPERTIES.getProperty(EXAMPLE_BASEDIR) != null) {
      setBaseDir(PROPERTIES.getProperty(EXAMPLE_BASEDIR));
    }
  }

  @Test
  public void testSetWipSuffix() throws Exception {
    FsConsumer consumer = createConsumer();

    String newSuffix = "new";
    consumer.setWipSuffix(newSuffix);
    assertEquals(consumer.getWipSuffix(), newSuffix);
    try {
      consumer.setWipSuffix("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      consumer.setWipSuffix(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
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
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(sc);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Test
  public void testConsumeImmediateEventPoller() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FsImmediateEventPoller());
    fs.setQuietInterval(new TimeInterval(500L, "MILLISECONDS"));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      LifecycleHelper.init(sc);
      createFiles(baseDir, ".xml", count);
      Thread.sleep(1000);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Test
  public void testConsumeWithAlphabeticSort() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setFileSorter(new AlphabeticAscending());
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
      AdaptrisMessage first = stub.getMessages().get(0);
      AdaptrisMessage last = stub.getMessages().get(9);
      String firstFilename = first.getMetadataValue(CoreConstants.ORIGINAL_NAME_KEY);
      String lastFilename = last.getMetadataValue(CoreConstants.ORIGINAL_NAME_KEY);
      assertTrue(firstFilename.compareTo(lastFilename) < 0);
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Test
  public void testConsumeWithLastModifiedSort() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setFileSorter(new LastModifiedAscending());
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 5;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      createFiles(baseDir, ".xml", count, 100);
      start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
      AdaptrisMessage first = stub.getMessages().get(0);
      AdaptrisMessage last = stub.getMessages().get(count - 1);
      String firstLastModified = first.getMetadataValue(CoreConstants.FILE_LAST_MODIFIED_KEY);
      String lastLastModified = last.getMetadataValue(CoreConstants.FILE_LAST_MODIFIED_KEY);
      assertTrue(Long.valueOf(firstLastModified).longValue() <= Long.valueOf(lastLastModified).longValue());
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Test
  public void testBug2233_ResetWipFilesOnNonExistentDirectory() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setCreateDirs(true);
    fs.setResetWipFiles(true);
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    LifecycleHelper.init(sc);
    LifecycleHelper.close(sc);
  }

  @Test
  public void testBug2233_ResetWipFilesOnInvalidDirectory() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    File dir = FsHelper.createFileReference(FsHelper.createUrlFromString(fs.getDestination().getDestination(), true));
    dir.getParentFile().mkdirs(); // attempting to fix the timing issues.
    dir.createNewFile(); // makes it a file, so it should be invalid now.
    fs.setCreateDirs(false);
    fs.setResetWipFiles(true);
    fs.setReacquireLockBetweenMessages(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    try {
      LifecycleHelper.init(sc);
    }
    catch (CoreException e) {
      log.warn(e.getMessage(), e);
      assertNull(e.getCause());
      assertTrue(e.getMessage().matches("^Failed to list files in.*Cannot reset WIP files"));
    }
  }

  @Test
  public void testBug2100_OriginalNameContainsWip() throws Exception {
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

  @Test
  public void testBug1675ConsumeWithSpacesInDir() throws Exception {
    String uniqueName = new GuidGenerator().safeUUID();

    String subDir = uniqueName + "Directory WithSpaces";
    String subDirEncoded = uniqueName + "Directory%20WithSpaces";
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDirEncoded);
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
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Test
  public void testConsumeWithFilter() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
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

  @Test
  public void testConsumeWithQuietPeriod() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
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

  @Test
  public void testConsumeWithResetWipFile() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setResetWipFiles(true);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      baseDir.mkdirs();
      createFiles(baseDir, ".xml.wip", count);
      LifecycleHelper.init(sc);
      Perl5FilenameFilter p5 = new Perl5FilenameFilter(".*\\.xml");
      assertTrue("Files renamed in " + baseDir.getCanonicalPath(), baseDir.listFiles((FilenameFilter) p5).length > 0);
    }
    finally {
      // sc.stop();
      LifecycleHelper.close(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Test
  public void testConsumeIgnoresWip() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    MockMessageListener stub = new MockMessageListener();
    FsConsumer fs = createConsumer(subDir);
    fs.setResetWipFiles(false);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      baseDir.mkdirs();
      createFiles(baseDir, ".xml", count);
      createFiles(baseDir, ".xml.wip", count);
      start(sc);
      waitForMessages(stub, count);

      Perl5FilenameFilter wip = new Perl5FilenameFilter(".*\\.wip");
      assertEquals(count, baseDir.listFiles((FilenameFilter) wip).length);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));

    }
  }

  @Test
  public void testConsumeFailInitialRename() throws Exception {
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
      LifecycleHelper.init(sc);
      List<File> createdFiles = createFiles(baseDir, ".xml", count);
      for (File file : createdFiles) {
        File wip = new File(file.getAbsolutePath() + ".wip");
        wip.createNewFile();
      }
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Test
  public void testRedmine481_SubDirInConsumeDirectory() throws Exception {
    String consumeDir = new GuidGenerator().safeUUID();
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));

    String subDir = parentDir.getCanonicalPath() + "/" + consumeDir + "/" + new GuidGenerator().safeUUID();
    File subDirectory = new File(subDir);
    subDirectory.mkdirs();
    FsConsumer fs = createConsumer(consumeDir);
    fs.setReacquireLockBetweenMessages(true);
    AtomicBoolean pollFired = new AtomicBoolean(false);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)).withPollerCallback(e -> {
      pollFired.set(true);
    }));
    File wipDirectory = new File(subDir + fs.getWipSuffix());
    MockMessageListener stub = new MockMessageListener(0);
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    try {
      start(sc);
      waitForPollCallback(pollFired);
      assertEquals(true, subDirectory.exists());
      assertEquals(true, subDirectory.isDirectory());
      assertEquals(false, wipDirectory.exists());
    }
    finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(BASE_KEY), consumeDir));
    }
  }

  @Override
  protected void assertMessages(List<AdaptrisMessage> list, int count, File[] remaining) {
    assertEquals("All files produced", count, list.size());
    assertEquals("All files consumed", 0, remaining.length);
    for (AdaptrisMessage m : list) {
      assertEquals(0, m.getSize());
      assertTrue(m.containsKey(CoreConstants.ORIGINAL_NAME_KEY));
      assertTrue(m.containsKey(CoreConstants.FILE_LAST_MODIFIED_KEY));
    }

  }

  @Override
  protected FsConsumer createConsumer(String subDir) {
    String destinationName = subDir == null ? PROPERTIES.getProperty(BASE_KEY) : PROPERTIES.getProperty(BASE_KEY) + "/" + subDir;
    FsConsumer fs = createConsumer();
    fs.setDestination(new ConfiguredConsumeDestination(destinationName));
    fs.setCreateDirs(true);
    return fs;
  }

  @Override
  protected FsConsumer createConsumer() {
    return new FsConsumer();
  }

  protected List<File> createFiles(File baseDir, String ext, int count) throws IOException, InterruptedException {
    return createFiles(baseDir, ext, count, 0);
  }

  protected List<File> createFiles(File baseDir, String ext, int count, long pause) throws IOException, InterruptedException {
    List<File> result = new ArrayList<File>();
    baseDir.mkdirs();
    for (int i = 0; i < count; i++) {
      result.add(File.createTempFile("FSC", ext, baseDir));
      if (pause > 0) {
        Thread.sleep(pause);
      }
    }
    return result;
  }

}
