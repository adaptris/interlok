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
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.List;
import org.junit.Test;

public class AlphabeticDescendingTest extends FileSorterCase {

  @Test
  public void testSort() throws Exception {
    AlphabeticDescending sorter = new AlphabeticDescending();
    List<File> files = createFiles(10);
    files = sorter.sort(files);
    log("Sorted", files);
    String lastFilename = String.format("%1$s-%2$03d%3$s", AlphabeticDescendingTest.class.getSimpleName(), 1, ".xml");
    String firstFilename = String.format("%1$s-%2$03d%3$s", AlphabeticDescendingTest.class.getSimpleName(), 10, ".xml");
    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertEquals(firstFilename, firstFile.getName());
    assertEquals(lastFilename, lastFile.getName());
  }
}
