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

package com.adaptris.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.adaptris.core.fs.FsHelper;
import com.adaptris.util.GuidGenerator;

public class StandardWorkerTest extends FsCase {

  // tests...
  public static final String BASE_KEY = "StandardWorkerTest._baseUrl";
  public static final String DATA = "dummy data";
  public static final String DEFAULT_FILTER_SUFFIX = ".text";
  public static final int FILE_CREATION_COUNT = 3;

  protected URL baseUrl;
  protected File baseDir;

  @Before
  public void setUp() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    baseUrl = new URL(PROPERTIES.getProperty(BASE_KEY) + "/" + subDir);
    baseDir = FsHelper.createFileReference(baseUrl);
    baseDir.mkdirs();
  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDirectory(baseDir);
    }
    catch (IOException e) {
    }
  }

  // write test files to file system
  protected String[] createTestFiles() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    List<String> list = new ArrayList<>();
    for (int i = 0; i < FILE_CREATION_COUNT; i++) {
      String fname = guid.safeUUID();
      File file = new File(baseDir, fname);
      try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
        pw.print(DATA);
      }
      list.add(fname);
    }
    return list.toArray(new String[0]);
  }

  protected Set<File> asFileArray(String[] files) {
    Set<File> result = new HashSet<>();
    for (String f : files) {
      result.add(new File(baseDir, f));
    }
    return result;
  }

  protected StandardWorker createWorker() {
    return new StandardWorker();
  }

  @Test
  public void testListFiles() throws Exception {
    File[] files = new File[] {
        new File("a"),
        new File("b"),
        new File("c")
    };
    File dir = Mockito.mock(File.class);
    Mockito.when(dir.canWrite()).thenReturn(true);
    Mockito.when(dir.exists()).thenReturn(true);
    Mockito.when(dir.canRead()).thenReturn(true);
    Mockito.when(dir.isDirectory()).thenReturn(true);
    Mockito.when(dir.listFiles((FileFilter) Mockito.any())).thenReturn(files);
    StandardWorker worker = createWorker();
    File[] listFiles = worker.listFiles(dir);
    assertEquals(3, listFiles.length);
    try {
      worker.listFiles(null);
      fail();
    } catch (FsException | IllegalArgumentException expected) {

    }
  }

  @Test
  public void testListFilesWith_NullResponse() throws Exception {
    File dir = Mockito.mock(File.class);
    Mockito.when(dir.canWrite()).thenReturn(true);
    Mockito.when(dir.exists()).thenReturn(true);
    Mockito.when(dir.canRead()).thenReturn(true);
    Mockito.when(dir.isDirectory()).thenReturn(true);
    Mockito.when(dir.listFiles((FileFilter) Mockito.any())).thenReturn(null);
    StandardWorker worker = createWorker();
    try {
      worker.listFiles(dir);
      fail();
    } catch (FsException expected) {

    }
  }

  @Test
  public void testGetFile() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();

    // valid file...
    byte[] bytes = worker.get(new File(baseDir, testFiles[0]));
    assertEquals(DATA, new String(bytes));
    // non-existent file...
    try {
      worker.get(new File(baseUrl.getPath() + "/whatever.txt"));
      fail("non-existent file didn't throw Exception");
    }
    catch (FsException e) { /* do nothing */
    }

    // directory...
    try {
      worker.get(new File(baseUrl.getPath()));
      fail("directory didn't throw Exception");
    }
    catch (FsException e) { /* do nothing */
    }
  }

  @Test
  public void testPutFile() throws Exception {
    FsWorker worker = createWorker();
    createTestFiles();
    String newFilename = new GuidGenerator().safeUUID();
    worker.put(DATA.getBytes(), new File(baseDir, newFilename));
    byte[] result = worker.get(new File(baseDir, newFilename));
    assertEquals(DATA, new String(result));

    File failingFile = Mockito.mock(File.class);
    Mockito.when(failingFile.canWrite()).thenReturn(true);
    Mockito.when(failingFile.getPath()).thenThrow(new RuntimeException());
    try {
      worker.put(DATA.getBytes(), failingFile);
      fail();
    } catch (FsException e) {
    }
  }

  @Test
  public void testPutFileExists() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();

    try {
      worker.put(DATA.getBytes(), new File(baseDir, testFiles[0]));
      fail();
    }
    catch (FsException e) {

    }
  }

  @Test
  public void testRenameFile() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();
    String newFilename = new GuidGenerator().safeUUID();

    worker.rename(new File(baseDir, testFiles[0]), new File(baseDir, newFilename));
    File[] files = worker.listFiles(baseDir);
    Set<File> set = new HashSet<>(Arrays.asList(files));
    assertTrue(set.contains(new File(FsHelper.createFileReference(baseUrl), newFilename)));
    try {
      worker.rename(new File(baseDir, testFiles[0]), new File(baseDir, testFiles[1]));
      fail("pre-exisitng new name doesn't cause exception");
    }
    catch (FsException e) { /* ok */
    }
    try {
      worker.rename(baseDir, new File(baseDir, "somename"));
      fail("old file name is dir doesn't cause exception");
    }
    catch (FsException e) { /* ok */
    }
  }

  @Test
  public void testDeleteFile() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();

    worker.delete(new File(baseDir, testFiles[0]));

    File[] files = worker.listFiles(baseDir);
    Set<File> set = new HashSet<>(Arrays.asList(files));
    assertTrue(!set.contains(new File(baseDir, testFiles[0])));
    worker.delete(new File(baseDir, "sthgelse"));
    try {
      worker.delete(baseDir);
      fail("no exception deleting directory");
    }
    catch (FsException e) {/* ok */
    }
    File newDir = new File(baseDir, "abc123");
    newDir.mkdirs();
    worker.delete(newDir);
  }

  @Test
  public void testIsWriteableDirectoryFile() throws Exception {
    FsWorker worker = createWorker();
    File newDir = new File(baseDir, "abc123");
    newDir.mkdir();
    assertTrue(worker.isWriteableDir(newDir));
    newDir.delete();
    assertFalse(worker.isWriteableDir(newDir));
    File file = new File(baseDir, "abc123");
    file.createNewFile();

    assertTrue(!worker.isWriteableDir(file));
  }

  @Test
  public void testToString() {
    FsWorker worker = createWorker();
    assertNotNull(worker.toString());
  }

  @Test
  public void testWrapException() {
    StandardWorker worker = createWorker();
    FsException exc= new FsException();
    assertEquals(exc, worker.wrapException(exc));
    FsFileNotFoundException e3 = new FsFileNotFoundException();
    assertEquals(e3, worker.wrapException(e3));
    assertEquals(FsException.class, worker.wrapException(new Exception()).getClass());
  }

  @Test
  public void testCheckWriteable() throws Exception {
    File nonExistent = Mockito.mock(File.class);
    Mockito.when(nonExistent.exists()).thenReturn(false);
    try {
      FsWorker.checkWriteable(nonExistent);
      fail();
    }
    catch (FsException expected) {

    }
    File unreadable = Mockito.mock(File.class);
    Mockito.when(unreadable.canRead()).thenReturn(false);
    Mockito.when(unreadable.exists()).thenReturn(true);
    Mockito.when(unreadable.canWrite()).thenReturn(true);
    try {
      FsWorker.checkWriteable(unreadable);
      fail();
    } catch (FsException expected) {

    }

    File unWriteable = Mockito.mock(File.class);
    Mockito.when(unWriteable.canWrite()).thenReturn(false);
    Mockito.when(unWriteable.exists()).thenReturn(true);
    Mockito.when(unWriteable.canRead()).thenReturn(true);
    try {
      FsWorker.checkWriteable(unWriteable);
      fail();
    } catch (FsException expected) {

    }
    try {
      FsWorker.checkWriteable(null);
      fail();
    } catch (FsException | IllegalArgumentException expected) {

    }

  }

  @Test
  public void testCheckExists() throws Exception {
    File nonExistent = Mockito.mock(File.class);
    Mockito.when(nonExistent.exists()).thenReturn(false);
    try {
      FsWorker.checkExists(nonExistent);
      fail();
    }
    catch (FsException expected) {

    }
    File exists = Mockito.mock(File.class);
    Mockito.when(exists.canRead()).thenReturn(false);
    Mockito.when(exists.exists()).thenReturn(true);
    Mockito.when(exists.canWrite()).thenReturn(true);
    FsWorker.checkExists(exists);
  }

  @Test
  public void testCheckNonExistent() throws Exception {
    File exists = Mockito.mock(File.class);
    Mockito.when(exists.exists()).thenReturn(true);
    try {
      FsWorker.checkNonExistent(exists);
      fail();
    }
    catch (FsException expected) {

    }
    File nonExistent = Mockito.mock(File.class);
    Mockito.when(nonExistent.canRead()).thenReturn(false);
    Mockito.when(nonExistent.exists()).thenReturn(false);
    Mockito.when(nonExistent.canWrite()).thenReturn(true);
    FsWorker.checkNonExistent(nonExistent);
  }

  @Test
  public void testCheckReadable() throws Exception {
    File cannotRead = Mockito.mock(File.class);
    Mockito.when(cannotRead.exists()).thenReturn(true);
    Mockito.when(cannotRead.canRead()).thenReturn(false);
    try {
      FsWorker.checkReadable(cannotRead);
      fail();
    }
    catch (FsException expected) {

    }
    File readable = Mockito.mock(File.class);
    Mockito.when(readable.canRead()).thenReturn(true);
    Mockito.when(readable.exists()).thenReturn(true);
    FsWorker.checkReadable(readable);
  }

  @Test
  public void testIsDirectory() throws Exception {
    File isFile = Mockito.mock(File.class);
    Mockito.when(isFile.exists()).thenReturn(true);
    Mockito.when(isFile.isDirectory()).thenReturn(false);
    try {
      FsWorker.isDirectory(isFile);
      fail();
    }
    catch (FsException expected) {

    }
    File dir = Mockito.mock(File.class);
    Mockito.when(dir.isDirectory()).thenReturn(true);
    Mockito.when(dir.exists()).thenReturn(true);
    FsWorker.isDirectory(dir);
  }

  @Test
  public void testIsFile() throws Exception {
    File isDirectory = Mockito.mock(File.class);
    Mockito.when(isDirectory.exists()).thenReturn(true);
    Mockito.when(isDirectory.isFile()).thenReturn(false);
    try {
      FsWorker.isFile(isDirectory);
      fail();
    }
    catch (FsException expected) {

    }
    File file = Mockito.mock(File.class);
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(file.exists()).thenReturn(true);
    FsWorker.isFile(file);
  }

  private class TmpFilter implements java.io.FileFilter {
    @Override
    public boolean accept(File file) {
      if (file.getName().endsWith(DEFAULT_FILTER_SUFFIX)) {
        return true;
      }

      return false;
    }
  }
}
