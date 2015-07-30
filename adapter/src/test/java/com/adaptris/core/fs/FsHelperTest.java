/*
 * $RCSfile: FsHelperTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/07/08 12:31:15 $
 * $Author: lchan $
 */
package com.adaptris.core.fs;

import java.io.File;
import java.net.URL;

import com.adaptris.core.BaseCase;

@SuppressWarnings("deprecation")
public class FsHelperTest extends BaseCase {

  public FsHelperTest(java.lang.String testName) {
    super(testName);
  }

  public void testUnixStyleFullURI() throws Exception {
    FsHelper.createUrlFromString("file:////home/fred");
  }

  public void testUnixStyleURI() throws Exception {
    URL url = FsHelper.createUrlFromString("/home/fred");
    assertEquals("fred", new File(url.toURI()).getName());
  }

  public void testWindowsFullURI() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:/home/fred");
    assertEquals("fred", new File(url.toURI()).getName());
  }

  public void testFullURIWithColons() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:/home/fred/d:/home/fred");
    assertEquals("fred", new File(url.toURI()).getName());
  }

  public void testCreateFileRef() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///home/fred");
    assertEquals(new File("/home/fred"), FsHelper.createFileReference(url));
    assertEquals(new File("/home/fred"), FsHelper.createFileReference(url, "UTF-8"));
  }

  public void testCreateFileRefWithSpaces() throws Exception {
    URL url = FsHelper
        .createUrlFromString("file:///home/directory%20with/some/spaces");
    File f = FsHelper.createFileReference(url);
    assertEquals(new File("/home/directory with/some/spaces"), f);
  }

  public void testCreateFileRefWithBackSlash() throws Exception {
    URL url = FsHelper.createUrlFromString("file:///c:\\home\\fred", true);
    assertEquals("fred", new File(url.toURI()).getName());
  }

  public void testWindowsURI() throws Exception {
    try {
      FsHelper.createUrlFromString("c:/home/fred");
    }
    catch (IllegalArgumentException e) {
      // This is expected. as c:/home/fred is in fact wrong.
      // Other exceptions are not falid though.
    }
  }

  public void testRelativeURL() throws Exception {
    File f = FsHelper.createFileReference(FsHelper.createUrlFromString("file://localhost/./fred", true));
    assertEquals("fred", f.getName());
    assertEquals("." + File.separator + "fred", f.getPath());
  }

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
      log.debug(FsHelper.createUrlFromString(urlString4));
      fail("no Exc. from invalid URI " + urlString4);
    }
    catch (Exception e) { /* okay */
    }

    // 5 - invalid relative URI...
    String urlString5 = "c:\\dir\\";

    try {
      log.debug(FsHelper.createUrlFromString(urlString5));
      fail("no Exc. from invalid URI " + urlString5);
    }
    catch (Exception e) { /* okay */
    }

    // 6 - invalid absolute URL...
    String urlString6 = "http://file/";

    try {
      log.debug(FsHelper.createUrlFromString(urlString6));
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