/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adaptris.core.fs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;
import org.apache.commons.io.FileUtils;
import org.apache.oro.io.Perl5FilenameFilter;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public class MovingDeletingFsConsumerTest extends FsConsumerCase {
  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   */
  public static final String EXAMPLE_BASEDIR = "NonDeletingFsConsumerExample.baseDir";


  @Override
  protected void configureExampleConfigBaseDir() {
    if (PROPERTIES.getProperty(EXAMPLE_BASEDIR) != null) {
      setBaseDir(PROPERTIES.getProperty(EXAMPLE_BASEDIR));
    }
  }

  @Test
  public void testConsume() throws Exception {
    final File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    final String subDir = new GuidGenerator().safeUUID();
    final File baseDir = new File(parentDir, subDir);
    final File procDir = new File(parentDir, "proc");

    final MockMessageListener stub = new MockMessageListener(10);

    final MovingNonDeletingFsConsumer fs = createConsumer(subDir, "testConsume");
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    fs.setProcessedPath(procDir.getAbsolutePath());

    final StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);

    final int count = 10;
    List<File> files = null;
    try {
      LifecycleHelper.init(sc);
      files = createFiles(baseDir, ".xml", count);
      LifecycleHelper.start(sc);
      waitForMessages(stub, count);
      assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    } catch (final Exception e) {
      log.warn(e.getMessage(), e);
      fail();
    } finally {
      stop(sc);
      if (files != null) {
        for (final File f : files) {
          boolean found = false;
          for (final String n : procDir.list()) {
            if (f.getName().equals(n)) {
              found = true;
            }
          }
          if (!found) {
            fail("Couldn't find file " + f.getName() + " in processed directory");
          }
        }
      }
      FileUtils.deleteQuietly(baseDir);
      FileUtils.deleteQuietly(procDir);
    }
  }

  /*
   * Hopefully much of the same setup for NonDeletingFsConsumer will apply (as MovingNonDeletingFsConsumer extends that class
   * anyway).
   */

  @Override
  protected void assertMessages(final List<AdaptrisMessage> list, final int count, final File[] remaining) {
    assertEquals("All files produced", count, list.size());
    assertEquals("All files left in dir", count, remaining.length);
    for (final AdaptrisMessage m : list) {
      assertTrue(m.containsKey(CoreConstants.ORIGINAL_NAME_KEY));
      assertTrue(m.containsKey(CoreConstants.FILE_LAST_MODIFIED_KEY));
    }

  }

  protected MovingNonDeletingFsConsumer createConsumer(final String subDir, final String threadname) {
    final String destinationName =
        subDir == null ? PROPERTIES.getProperty(BASE_KEY) : PROPERTIES.getProperty(BASE_KEY) + "/" + subDir;

    final MovingNonDeletingFsConsumer fs = new MovingNonDeletingFsConsumer();
    fs.setBaseDirectoryUrl(destinationName);
    fs.setReacquireLockBetweenMessages(true);
    fs.setCreateDirs(true);
    return fs;
  }

  @Override
  protected MovingNonDeletingFsConsumer createConsumer(final String subDir) {
    return createConsumer(subDir, null);
  }

  @Override
  protected MovingNonDeletingFsConsumer createConsumer() {
    return new MovingNonDeletingFsConsumer();
  }

  private static List<File> createFiles(final File baseDir, final String ext, final int count) throws IOException {
    baseDir.mkdirs();
    final List<File> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      result.add(File.createTempFile("FSC", ext, baseDir));
    }
    return result;
  }
}
