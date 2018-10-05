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
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.mockito.Mockito;


/**
 */
public class OverwriteIfExistsWorkerTest extends StandardWorkerTest {


  @Override
  protected OverwriteIfExistsWorker createWorker() {
    return new OverwriteIfExistsWorker();
  }

  @Override
  @Test
  public void testPutFileExists() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();
    worker.put(DATA.getBytes(), new File(baseDir, testFiles[0]));
    // So it should have overwritten appended it.
    byte[] readBytes = worker.get(new File(baseDir, testFiles[0]));
    assertEquals(DATA, new String(readBytes));
  }

  @Test
  public void testPutFile_Does_Not_Exist() throws Exception {
    OverwriteIfExistsWorker worker = createWorker();
    File failingFile = Mockito.mock(File.class);
    Mockito.when(failingFile.exists()).thenReturn(false);
    Mockito.when(failingFile.delete()).thenReturn(true);
    try {
      worker.put(DATA.getBytes(), failingFile);
      fail();
    } catch (FsException expected) {

    }
  }

  @Test
  public void testPutFile_Does_Not_Delete() throws Exception {
    OverwriteIfExistsWorker worker = createWorker();
    File failingFile = Mockito.mock(File.class);
    Mockito.when(failingFile.exists()).thenReturn(true);
    Mockito.when(failingFile.delete()).thenReturn(false);
    try {
      worker.put(DATA.getBytes(), failingFile);
      fail();
    } catch (FsException expected) {

    }
  }

}
