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

import java.io.File;
import java.util.List;

public class SizeDescendingTest extends FileSorterCase {

  public SizeDescendingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    SizeDescending sorter = new SizeDescending();
    List<File> files = createFiles(10);
    files = sorter.sort(files);
    log("Sorted", files);

    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertTrue(firstFile.length() > lastFile.length());
  }
}
