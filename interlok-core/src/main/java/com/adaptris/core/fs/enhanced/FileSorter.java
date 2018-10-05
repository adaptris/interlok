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

/**
 * Interface that allows FsConsumer style implementations to sort the list of files before processing.
 *
 * @author lchan
 * 
 */
public interface FileSorter {

  /**
   * Sort the list of files that need to be processed.
   *
   * @param unsorted an unsorted list of files.
   * @return the sorted list.
   */
  List<File> sort(List<File> unsorted);

}
