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

package com.adaptris.filetransfer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.BaseCase;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.util.GuidGenerator;

import junit.framework.TestCase;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class FtpCase extends TestCase {
  protected static final String FILE_TEXT = "The quick brown fox jumps over the lazy dog";

  protected Properties config;
  protected transient Log logR = LogFactory.getLog(this.getClass());

  public FtpCase(String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    try {
      initialiseConfig();
    }
    catch (Exception e) {
      logR.trace(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testLogin() throws Exception {
    if (areTestsEnabled()) {

      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testLs() throws Exception {
    if (areTestsEnabled()) {

      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        String[] files = client.dir(getRemoteGetDirectory());
        for (int i = 0; i < files.length; i++) {
          logR.debug(files[i]);
        }
        assertTrue(files.length > 0);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testLsFull() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        String[] files = client.dir(getRemoteGetDirectory(), true);
        for (int i = 0; i < files.length; i++) {
          logR.debug(files[i]);
          // logR.debug(HexDump.parse(files[i].getBytes()));
        }
        assertTrue(files.length > 0);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testLsWithFileFilter() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        String[] files = client.dir(getRemoteGetDirectory(), getRemoteGetFileFilter());
        assertEquals("Should only be one file matching " + getRemoteGetFilterString(), 1, files.length);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testLsWithNullFileFilter() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        String[] files = client.dir(getRemoteGetDirectory(), (FileFilter) null);
        assertTrue(files.length > 1);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void testLsWithFilenameFilter() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        String[] files = client.dir(getRemoteGetDirectory(), getRemoteGetFilenameFilter());
        assertEquals("Should only be one file matching " + getRemoteGetFilterString(), 1, files.length);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void testLsWithNullFilenameFilter() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        String[] files = client.dir(getRemoteGetDirectory(), (FilenameFilter) null);
        assertTrue(files.length > 1);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testCdThenLs() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());

        FileTransferClient client = connectClientImpl();
        client.chdir(getRemoteGetDirectory());
        String[] files = client.dir();
        assertTrue(files.length > 0);
        client.disconnect();
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testGet_ToLocalFile() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        String filename = getRemoteGetFilename();
        FileTransferClient client = connectClientImpl();
        File localFile = TempFileUtils.createTrackedFile(client);
        client.chdir(getRemoteGetDirectory());
        client.get(localFile.getCanonicalPath(), filename);
        String s = FileUtils.readFileToString(localFile, Charset.defaultCharset()).trim();
        assertEquals("File contents", s, FILE_TEXT);
        client.disconnect();
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testGet() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName("testGet");
        String filename = getRemoteGetFilename();
        FileTransferClient client = connectClientImpl();
        client.chdir(getRemoteGetDirectory());
        byte[] b = client.get(filename);
        String s = new String(b).trim();
        assertEquals("File contents", s, FILE_TEXT);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testPut() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        String filename = getRemotePutFilename();
        FileTransferClient client = connectClientImpl();
        client.chdir(getRemotePutDirectory());
        client.put(FILE_TEXT.getBytes(), filename);
        client.disconnect();
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testPut_append() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        String filename = getRemotePutFilename();
        FileTransferClient client = connectClientImpl();
        client.chdir(getRemotePutDirectory());
        client.put(FILE_TEXT.getBytes(), filename, true);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testPut_FromFile() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        String filename = getRemotePutFilename();
        FileTransferClient client = connectClientImpl();
        File localFile = TempFileUtils.createTrackedFile(client);
        client.chdir(getRemotePutDirectory());
        FileUtils.write(localFile, FILE_TEXT, Charset.defaultCharset());
        client.put(localFile.getCanonicalPath(), filename);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testRename() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        String filename = getRemotePutFilename();
        FileTransferClient client = connectClientImpl();
        client.chdir(getRemotePutDirectory());
        client.put(FILE_TEXT.getBytes(), filename);
        String newFilename = new GuidGenerator().safeUUID();
        client.rename(filename, newFilename);
        client.delete(newFilename);
        client.disconnect();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }
  public void testGetLastModifiedWithRelativePath() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());

        FileTransferClient client = connectClientImpl();
        client.chdir(getRemoteGetDirectory());
        long mtime = client.lastModified(getRemoteGetFilename());
        logR.debug("testGetLastModifiedWithRelativePath : " + new Date(mtime));
        client.disconnect();
        assertTrue("Comparing Dates", new Date().after(new Date(mtime)));
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testGetLastModifiedDateWithRelativePath() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());

        FileTransferClient client = connectClientImpl();
        client.chdir(getRemoteGetDirectory());
        Date mtime = client.lastModifiedDate(getRemoteGetFilename());
        logR.debug("testGetLastModifiedWithRelativePath : " + mtime);
        client.disconnect();
        assertTrue("Comparing Dates", new Date().after(mtime));
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testGetLastModifiedWithAbsolutePath() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(getName());
        FileTransferClient client = connectClientImpl();
        long mtime = client.lastModified(getRemoteGetDirectory() + "/"
            + getRemoteGetFilename());
        logR.debug("testGetLastModifiedWithAbsolutePath : " + new Date(mtime));
        client.disconnect();
        assertTrue(new Date().after(new Date(mtime)));
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testGetLastModifiedDateWithAbsolutePath() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName(
            "testGetLastModifiedDateWithAbsolutePath");
        FileTransferClient client = connectClientImpl();
        Date mtime = client.lastModifiedDate(getRemoteGetDirectory() + "/"
            + getRemoteGetFilename());
        logR.debug("testGetLastModifiedDateWithAbsolutePath : " + mtime);
        client.disconnect();
        assertTrue(new Date().after(mtime));
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testDelete() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName("testDelete");
        FileTransferClient client = connectClientImpl();
        client.chdir(getRemotePutDirectory());
        client.delete(getRemotePutFilename());
        client.disconnect();
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testMkdirThenRmDir() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      String dirname = new GuidGenerator().getUUID().replaceAll(":", "")
          .replaceAll("-", "");
      try {
        Thread.currentThread().setName("testMkdirThenRmDir");
        FileTransferClient client = connectClientImpl();
        client.chdir(getRemotePutDirectory());
        client.mkdir(dirname);
        client.chdir(dirname);
        client.chdir("..");
        client.rmdir(dirname);
        client.disconnect();
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  public void testCdBadDirectory() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName("testCdBadDirectory");
        FileTransferClient client = connectClientImpl();

        try {
          Random r = new Random();
          String dir = getRemotePutDirectory() + "/" + r.nextInt();
          client.chdir(dir);
          fail("CD to " + dir + " should not work");
        }
        catch (Exception e) {
          client.disconnect();
        }
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  protected void initialiseConfig() throws IOException {
    if (config == null) {
      config = BaseCase.PROPERTIES;
    }
    if (config == null) throw new IOException("No Configuration available");
  }

  protected abstract String getRemoteGetDirectory() throws IOException;

  protected abstract String getRemotePutDirectory() throws IOException;

  protected abstract String getRemoteGetFilename() throws IOException;

  protected abstract String getRemotePutFilename() throws IOException;

  protected abstract FilenameFilter getRemoteGetFilenameFilter()
      throws IOException;

  protected abstract FileFilter getRemoteGetFileFilter() throws IOException;

  protected abstract FileTransferClient connectClientImpl() throws Exception;

  protected abstract String getRemoteGetFilterString();

  protected boolean areTestsEnabled() {
    return Boolean.parseBoolean(config
        .getProperty("ftp.tests.enabled", "false"));
  }
}
