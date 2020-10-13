package com.adaptris.core.fs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import org.junit.Test;
import com.adaptris.core.stubs.TempFileUtils;

public class IsFileFilterTest {

  @Test
  public void testIsFile() throws Exception {
    IsFileFilter filter = new IsFileFilter();
    File file = TempFileUtils.createTrackedFile(filter, () -> "Hello World");
    assertTrue(filter.accept(file));
  }

  @Test
  public void testIsDirectory() throws Exception {
    IsFileFilter filter = new IsFileFilter();
    File dir = TempFileUtils.createTrackedDir(filter);
    assertFalse(filter.accept(dir));
  }
}
