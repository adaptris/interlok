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

package com.adaptris.core.fs.enhanced;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import com.adaptris.interlok.cloud.RemoteFile;

public class LastModifiedAscendingTest extends FileSorterCase {

  @Test
  public void testSort() throws Exception {
    LastModifiedAscending sorter = new LastModifiedAscending();
    List<File> files = buildFileList(10);
    files = sorter.sort(files);
    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertTrue(lastFile.lastModified() >= firstFile.lastModified());
  }


  private List<File> buildFileList(int count) {
    List<File> result = new ArrayList<File>();
    long now = System.currentTimeMillis();
    Random r = ThreadLocalRandom.current();
    for (int i = 0; i < count; i++) {
      long lastModified = now + r.nextInt(10000);
      result.add(new RemoteFile.Builder().setPath("file_a" + i).setLastModified(lastModified).build());
      result.add(new RemoteFile.Builder().setPath("file_b" + i).setLastModified(lastModified).build());
    }
    return result;
  }
}
