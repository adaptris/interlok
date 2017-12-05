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

import java.io.File;
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
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;

import com.adaptris.core.fs.FsHelper;
import com.adaptris.util.GuidGenerator;

public class StandardWorkerTest extends FsCase {

  public StandardWorkerTest(java.lang.String testName) {
    super(testName);
  }

  // tests...
  public static final String BASE_KEY = "StandardWorkerTest._baseUrl";
  public static final String DATA = "dummy data";
  public static final String DEFAULT_FILTER_SUFFIX = ".text";
  public static final int FILE_CREATION_COUNT = 3;

  protected URL baseUrl;
  protected File baseDir;

  @Override
  protected void setUp() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    baseUrl = new URL(PROPERTIES.getProperty(BASE_KEY) + "/" + subDir);
    baseDir = FsHelper.createFileReference(baseUrl);
    baseDir.mkdirs();
  }

  @Override
  protected void tearDown() {
    try {
      FileUtils.deleteDirectory(baseDir);
    }
    catch (IOException e) {
    }
  }

  // write test files to file system
  protected String[] createTestFiles() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < FILE_CREATION_COUNT; i++) {
      String fname = guid.safeUUID();
      PrintWriter pw = null;
      try {
        File file = new File(baseDir, fname);
        pw = new PrintWriter(new FileWriter(file));
        pw.print(DATA);
      }
      finally {
        IOUtils.closeQuietly(pw);
      }
      list.add(fname);
    }
    return list.toArray(new String[0]);
  }

  protected Set<File> asFileArray(String[] files) {
    Set<File> result = new HashSet<File>();
    for (String f : files) {
      result.add(new File(baseDir, f));
    }
    return result;
  }

  protected FsWorker createWorker() {
    return new StandardWorker();
  }

  public void testListFiles() throws Exception {
    String[] testFiles = createTestFiles();
    Set<File> expected = asFileArray(testFiles);
    FsWorker worker = createWorker();
    try {
      worker.listFiles(null);
    }
    catch (FsException e) {

    }
    assertEquals("listFiles without filter", expected, new HashSet(Arrays.asList(worker.listFiles(baseDir))));
    assertEquals("listFiles NullFilter ", expected, new HashSet(Arrays.asList(worker.listFiles(baseDir, null))));
  }

  public void testListFilesWithFilter() throws Exception {
    String[] testFiles = createTestFiles();
    File filteredFilename = File.createTempFile(this.getClass().getSimpleName(), DEFAULT_FILTER_SUFFIX, baseDir);
    Set<File> expected = new HashSet<File>(Arrays.asList(new File[]
    {
      filteredFilename
    }));
    FsWorker worker = createWorker();
    assertEquals("listFiles with Filter ", expected, new HashSet(Arrays.asList(worker.listFiles(baseDir, new TmpFilter()))));
  }

  public void testListFilesNonExistentDirectory() throws Exception {
    String[] testFiles = createTestFiles();
    FsWorker worker = createWorker();
    try {
      worker.listFiles(new File(baseDir, "fred"));
      fail();
    }
    catch (FsException expected) {
    }
  }

  public void testListFilesNotDirectory() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();
    try {
      worker.listFiles(new File(baseDir, testFiles[0]));
      fail();
    }
    catch (FsException expected) {
    }
  }

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

  public void testPutFile() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();
    String newFilename = new GuidGenerator().safeUUID();
    worker.put(DATA.getBytes(), new File(baseDir, newFilename));
    byte[] result = worker.get(new File(baseDir, newFilename));
    assertEquals(DATA, new String(result));
  }

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

  public void testRenameFile() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();
    String newFilename = new GuidGenerator().safeUUID();

    worker.rename(new File(baseDir, testFiles[0]), new File(baseDir, newFilename));
    File[] files = worker.listFiles(baseDir);
    Set set = new HashSet(Arrays.asList(files));
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

  public void testDeleteFile() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();

    worker.delete(new File(baseDir, testFiles[0]));

    File[] files = worker.listFiles(baseDir);
    Set set = new HashSet(Arrays.asList(files));
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

  public void testToString() {
    FsWorker worker = createWorker();
    assertNotNull(worker.toString());
  }

  public void testGetFileNoWriteAccess() throws Exception {
    File file = Mockito.mock(File.class);
    Mockito.when(file.canWrite()).thenReturn(false);
    FsWorker worker = createWorker();
    try {
      worker.get(file);
      fail();
    }
    catch (FsException expected) {

    }

  }

  public void testGetFileNoReadAccess() throws Exception {
    File file = Mockito.mock(File.class);
    Mockito.when(file.canRead()).thenReturn(false);
    FsWorker worker = createWorker();
    try {
      worker.get(file);
      fail();
    }
    catch (FsException expected) {

    }
  }

  private class TmpFilter implements java.io.FileFilter {
    public boolean accept(File file) {
      if (file.getName().endsWith(DEFAULT_FILTER_SUFFIX)) {
        return true;
      }

      return false;
    }
  }
}
