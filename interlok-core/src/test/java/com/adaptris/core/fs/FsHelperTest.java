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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.junit.Test;

@SuppressWarnings("deprecation")
public class FsHelperTest extends FsHelper {

  @Test
  public void testUnixStyleFullURI() throws Exception {
    FsHelper.createUrlFromString("file:////home/fred");
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
  public void testRelativeURL() throws Exception {
    File f = FsHelper.toFile("file://localhost/./fred");
    assertEquals("fred", f.getName());
    assertEquals("." + File.separator + "fred", f.getPath());
  }

  @Test
  public void testCreateUrlFromString() throws Exception {
    // 1 - valid absolute URL...
    String urlString = "file:///c:/tmp/";
    URL url = FsHelper.createUrlFromString(urlString);

    // can't use this - the number of slashes changes to one...
    // assertTrue(urlString.equals(url.toString()));

    assertTrue("protocol", "file".equals(url.getProtocol()));
    assertTrue("path " + url.getPath(), "/c:/tmp/".equals(url.getPath()));

    // 2 - valid absolute URL with 1 slash...
    String urlString2 = "file:/c:/tmp/";
    URL url2 = FsHelper.createUrlFromString(urlString2);

    assertTrue("protocol", "file".equals(url2.getProtocol()));
    assertTrue("path " + url.getPath(), "/c:/tmp/".equals(url2.getPath()));

    // 3 - valid relative URI...
    String urlString3 = "../dir/";
    URL url3 = FsHelper.createUrlFromString(urlString3);

    assertTrue("protocol", "file".equals(url3.getProtocol()));

    // either configure where you are running or rewrite method to obtain sthg
    // to test against!
    // assertTrue("path " + url.getPath(),
    // "/c:/tmp/dir/".equals(url3.getPath()));

    // 4 - invalid relative URI...
    String urlString4 = "..\\dir\\";

    try {
      FsHelper.createUrlFromString(urlString4);
      fail("no Exc. from invalid URI " + urlString4);
    }
    catch (Exception e) { /* okay */
    }

    // 5 - invalid relative URI...
    String urlString5 = "c:\\dir\\";

    try {
      FsHelper.createUrlFromString(urlString5);
      fail("no Exc. from invalid URI " + urlString5);
    }
    catch (Exception e) { /* okay */
    }

    // 6 - invalid absolute URL...
    String urlString6 = "http://file/";

    try {
      FsHelper.createUrlFromString(urlString6);
      fail("no Exc. from invalid URI " + urlString6);
    }
    catch (Exception e) { /* okay */
    }

    // 7 - invalid absolute URL...
    String urlString7 = "file:\\\file\\";

    try {
      FsHelper.createUrlFromString(urlString7);
      fail("no Exc. from invalid URI " + urlString7);
    }
    catch (Exception e) { /* okay */
    }
  }
}
