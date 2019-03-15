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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.oro.io.AwkFilenameFilter;
import org.junit.Test;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.StandardWorker;

@SuppressWarnings("deprecation")
public class FsHelperTest extends FsHelper {

  @Test
  public void testUnixStyleFullURI() throws Exception {
    assertNotNull(FsHelper.createUrlFromString("file:////home/fred"));
  }

  @Test
  public void testUnixStyleURI() throws Exception {
    URL url = FsHelper.createUrlFromString("/home/fred");
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testWindowsFullURI() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:/home/fred");
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testFullURIWithColons() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:/home/fred/d:/home/fred");
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testCreateFileRef() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///home/fred");
    assertEquals(new File("/home/fred"), FsHelper.createFileReference(url));
    assertEquals(new File("/home/fred"), FsHelper.createFileReference(url, "UTF-8"));
  }

  @Test
  public void testCreateFileRefWithSpaces() throws Exception {
    URL url = FsHelper
        .createUrlFromString("file:///home/directory%20with/some/spaces");
    File f = FsHelper.createFileReference(url);
    assertEquals(new File("/home/directory with/some/spaces"), f);
  }

  @Test
  public void testCreateFileRefWithBackSlash() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:\\home\\fred", true);
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testWindowsURI() throws Exception {
    try {
      FsHelper.createUrlFromString("c:/home/fred");
    }
    catch (IllegalArgumentException e) {
      // This is expected. as c:/home/fred is in fact wrong.
      // Other exceptions are not falid though.
    }
  }

  @Test
  public void testToFile() throws Exception {
    File f = FsHelper.toFile("file://localhost/./fred");
    assertEquals("fred", f.getName());
    assertEquals("." + File.separator + "fred", f.getPath());
    File f2 = FsHelper.toFile("c:/home/fred");
    assertEquals("fred", f2.getName());
    assertNotNull(f2.getParentFile());
    File f3 = FsHelper.toFile("/home/fred");
    assertEquals("fred", f3.getName());
    // "/home"
    assertNotNull(f3.getParentFile());
    // should be "/"
    assertNotNull(f3.getParentFile().getParentFile());
    assertNull(f3.getParentFile().getParentFile().getParentFile());
  }


  @Test
  public void testRelativeURL() throws Exception {
    File f = FsHelper.toFile("file://localhost/./fred");
    assertEquals("fred", f.getName());
    assertEquals("." + File.separator + "fred", f.getPath());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testCreateUrlFromString() throws Exception {

    String urlString = "file:///c:/tmp/";
    URL url = FsHelper.createUrlFromString(urlString, true);

    assertTrue("protocol", "file".equals(url.getProtocol()));
    assertTrue("path " + url.getPath(), "/c:/tmp/".equals(url.getPath()));

    String urlString2 = "file:/c:/tmp/";
    URL url2 = FsHelper.createUrlFromString(urlString2, true);
    assertTrue("protocol", "file".equals(url2.getProtocol()));
    assertTrue("path " + url.getPath(), "/c:/tmp/".equals(url2.getPath()));

    String urlString3 = "../dir/";
    URL url3 = FsHelper.createUrlFromString(urlString3, true);
    assertTrue("protocol", "file".equals(url3.getProtocol()));

    tryExpectingException(() -> {
      FsHelper.createUrlFromString("..\\dir\\");
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString("c:\\dir\\");
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString("http://file/");
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString("file:\\\file\\");
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString(null, true);
    });
  }


  @Test
  public void testCreateFilter() throws Exception {
    FileFilter filter = createFilter("", RegexFileFilter.class.getCanonicalName());
    // no op filter.
    assertTrue(filter.accept(new File("build.gradle")));
    assertNotEquals(RegexFileFilter.class, filter.getClass());
    filter = createFilter(".*", RegexFileFilter.class.getCanonicalName());
    assertEquals(RegexFileFilter.class, filter.getClass());
    assertTrue(filter.accept(new File("build.gradle")));

    // Just to fire the warning.
    assertEquals(AwkFilenameFilter.class,
        createFilter(".*", AwkFilenameFilter.class.getCanonicalName()).getClass());
  }

  @Test
  public void testRenameFile() throws Exception {
    FsWorker worker = new StandardWorker();
    File src = TempFileUtils.createTrackedFile(this);
    src.createNewFile();
    File renamed = renameFile(src, ".wip", new StandardWorker());
    assertTrue(renamed.exists());
    assertFalse(src.exists());
  }

  @Test
  public void testRenameFile_AlreadyExists() throws Exception {
    File src = TempFileUtils.createTrackedFile(this);
    src.createNewFile();

    File wipFile = new File(src.getCanonicalPath() + ".wip");
    wipFile.createNewFile();
    TempFileUtils.trackFile(wipFile, this);

    File renamed = renameFile(src, ".wip", new StandardWorker());
    // Should have a timestamp addition.
    assertNotEquals(wipFile, renamed);
    assertFalse(src.exists());
    assertTrue(wipFile.exists());
    assertTrue(renamed.exists());
  }


  private void tryExpectingException(Attempt t) {
    try {
      t.tryIt();
      fail();
    } catch (Exception expected) {

    }
  }

  @FunctionalInterface
  private interface Attempt {
    void tryIt() throws Exception;
  }
}
