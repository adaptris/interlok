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

public class SizeAscendingTest extends FileSorterCase {

  @Test
  public void testSort() throws Exception {
    SizeAscending sorter = new SizeAscending();
    List<File> files = buildFileList(10);
    files = sorter.sort(files);
    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertTrue(lastFile.length() >= firstFile.length());
  }


  private List<File> buildFileList(int count) {
    List<File> result = new ArrayList<File>();
    Random r = ThreadLocalRandom.current();
    for (int i = 0; i < count; i++) {
      long size = r.nextInt(10000);
      result.add(new RemoteFile.Builder().setPath("file_a" + i).setLength(size).build());
      result.add(new RemoteFile.Builder().setPath("file_b" + i).setLength(size).build());
    }
    return result;
  }

}
