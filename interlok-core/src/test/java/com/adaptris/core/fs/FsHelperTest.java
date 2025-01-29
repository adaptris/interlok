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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.oro.io.AwkFilenameFilter;
import org.junit.jupiter.api.Test;

import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.StandardWorker;

@SuppressWarnings("deprecation")
public class FsHelperTest extends FsHelper {

  @Test
  public void testUnixStyleFullURI() throws Exception {
    assertNotNull(FsHelper.createUrlFromString("file:////home/fred", true));
  }

  @Test
  public void testUnixStyleAbsoluteURI() throws Exception {
    URL url = FsHelper.createUrlFromString("/home/fred", true);
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testUnixStyleRelativeURI() throws Exception {
    URL url = FsHelper.createUrlFromString("./home/fred", true);
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testWindowsFullURI() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:/home/fred", true);
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testFullURIWithColons() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:/home/fred/d:/home/fred", true);
    assertEquals("fred", new File(url.toURI()).getName());
  }

  @Test
  public void testCreateFileRef() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///home/fred", true);
    assertEquals(new File("/home/fred"), FsHelper.createFileReference(url));
    assertEquals(new File("/home/fred"), FsHelper.createFileReference(url, "UTF-8"));
  }

  @Test
  public void testCreateFileRefWithSpaces() throws Exception {
    URL url = FsHelper
        .createUrlFromString("file:///home/directory%20with/some/spaces", true);
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
    URL url = FsHelper.createUrlFromString("c:/home/fred", true);
    assertEquals("fred", new File(url.toURI()).getName());
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

    File f4 = FsHelper.toFile("build.gradle");
    assertEquals("build.gradle", f4.getName());
    // will be "/"
    assertNotNull(f4.getParentFile());
    assertNull(f4.getParentFile().getParentFile());

    File f5 = FsHelper.toFile("./build.gradle");
    assertEquals("build.gradle", f4.getName());
    // will be "."
    assertNotNull(f5.getParentFile());
    assertNull(f5.getParentFile().getParentFile());

    File f6 = FsHelper.toFile("http://localhost/./fred");
    assertEquals("fred", f6.getName());
  }


  @Test
  public void testRelativeAndOtherURLs() throws Exception {
    for (List<String> data : List.of(
            List.of("exceptions/duplicate", "duplicate", "." + File.separator + "exceptions" + File.separator + "duplicate"),
            List.of("./exceptions/duplicate", "duplicate", "." + File.separator + "exceptions" + File.separator + "duplicate"),
            List.of("../exceptions/duplicate", "duplicate", ".." + File.separator + "exceptions" + File.separator + "duplicate"),
            List.of("file://localhost/./fred", "fred", "." + File.separator + "fred"),
            List.of("file://localhost/./fred/wilma", "wilma", "." + File.separator + "fred" + File.separator + "wilma"),
            List.of("file:./a/b.xml", "b.xml", "." + File.separator + "a" + File.separator + "b.xml"),
            List.of("file://./a1/b1.xml", "b1.xml", "." + File.separator + "a1" + File.separator + "b1.xml"),
            List.of("file://../a2/b2.xml", "b2.xml", ".." + File.separator + "a2" + File.separator + "b2.xml"),
            List.of("file:///./a3/b3.xml", "b3.xml", "." + File.separator + "a3" + File.separator + "b3.xml"),
            List.of("file:///a4/b4.xml", "b4.xml", File.separator + "a4" + File.separator + "b4.xml"),
            List.of("file:////a5/b5.xml", "b5.xml", File.separator + "a5" + File.separator + "b5.xml")
    )) {
      File f = FsHelper.toFile(data.get(0));
      assertEquals(data.get(1), f.getName());
      assertEquals(data.get(2), f.getPath());
    }
  }

  @Test
  public void testCreateUrlFromString() throws Exception {

    String urlString = "file:///c:/tmp/";
    URL url = FsHelper.createUrlFromString(urlString, true);

    assertTrue("file".equals(url.getProtocol()));
    assertTrue("/c:/tmp/".equals(url.getPath()));

    String urlString2 = "file:/c:/tmp/";
    URL url2 = FsHelper.createUrlFromString(urlString2, true);
    assertTrue("file".equals(url2.getProtocol()));
    assertTrue("/c:/tmp/".equals(url2.getPath()));

    String urlString3 = "../dir/";
    URL url3 = FsHelper.createUrlFromString(urlString3, true);
    assertTrue("file".equals(url3.getProtocol()));
    assertTrue("/../dir/".equals(url3.getPath()));

    String urlString4 = "..\\dir\\";
    URL url4 = FsHelper.createUrlFromString(urlString4, true);
    assertTrue("file".equals(url4.getProtocol()));
    assertTrue("/../dir/".equals(url4.getPath()));

    String urlString5 = "c:\\dir\\";
    URL url5 = FsHelper.createUrlFromString(urlString5, true);
    assertTrue("file".equals(url5.getProtocol()));
    assertEquals("/c:/dir/", url5.getPath());

    String urlString6 = "D:\\dir\\";
    URL url6 = FsHelper.createUrlFromString(urlString6, true);
    assertTrue("file".equals(url6.getProtocol()));
    assertEquals("/D:/dir/", url6.getPath());

    tryExpectingException(() -> {
      FsHelper.createUrlFromString("..\\dir\\", false);
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString("c:\\dir\\", false);
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString("http://file/", true);
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString("5://file/", true);
    });
    tryExpectingException(() -> {
      FsHelper.createUrlFromString("file:\\\file\\", true);
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
