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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.adaptris.core.stubs.TempFileUtils;

public class NewerThanTest {

  @Test
  public void testNewerThan() throws Exception {
    NewerThan filter = new NewerThan("-PT30S");
    File file = writeFile(TempFileUtils.createTrackedFile(filter));
    assertTrue(filter.accept(file));
  }

  @Test
  public void testNewerThanInTheFuture() throws Exception {
    NewerThan filter = new NewerThan("PT1H");
    File file = writeFile(TempFileUtils.createTrackedFile(filter));
    assertFalse(filter.accept(file));
  }

  @Test
  public void testBadDuration() throws Exception {
    NewerThan filter = new NewerThan("-PXXX");
    File file = writeFile(TempFileUtils.createTrackedFile(filter));
    assertFalse(filter.accept(file));
  }

  @Test
  public void testAbsolute() throws Exception {
    // Make it 2 days ago.
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_YEAR, -2);
    NewerThan filter = new NewerThan("" + c.getTime().getTime());
    File file = writeFile(TempFileUtils.createTrackedFile(filter));
    assertTrue(filter.accept(file));
  }

  private File writeFile(File f) throws IOException {
    FileUtils.write(f, "Hello World", Charset.defaultCharset());
    return f;
  }
}
