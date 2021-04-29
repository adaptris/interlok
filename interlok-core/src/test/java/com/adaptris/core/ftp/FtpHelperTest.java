package com.adaptris.core.ftp;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FtpHelperTest extends FtpHelper {

  private static final String FILENAME = "filename";
  private static final String UNIX_DIR = "/home/interlok/path/to";
  private static final String WINDOWS_DIR = "\\home\\interlok\\path\\to";
  private static final String UNIX_PATH = UNIX_DIR + "/" + FILENAME;
  private static final String WINDOWS_PATH = WINDOWS_DIR + "\\" + FILENAME;

  @Test
  public void testGetFilename() {
    String filename = getFilename(UNIX_PATH);
    assertEquals(FILENAME, filename);

    filename = getFilename(UNIX_PATH, true);
    assertNotEquals(FILENAME, filename);
    assertEquals(UNIX_PATH, filename);

    filename = getFilename(WINDOWS_PATH, true);
    assertEquals(FILENAME, filename);

    filename = getFilename(WINDOWS_PATH, false);
    assertNotEquals(FILENAME, filename);
    assertEquals(WINDOWS_PATH, filename);
  }

  @Test
  public void testGetDirectory() {
    String directory = getDirectory(UNIX_PATH);
    assertEquals(UNIX_DIR, directory);

    directory = getDirectory(UNIX_PATH, true);
    assertNotEquals(UNIX_DIR, directory);
    assertEquals(UNIX_PATH, directory);

    directory = getDirectory(WINDOWS_PATH, true);
    assertEquals(WINDOWS_DIR, directory);

    directory = getDirectory(WINDOWS_PATH, false);
    assertNotEquals(WINDOWS_DIR, directory);
    assertEquals(WINDOWS_PATH, directory);
  }
}
