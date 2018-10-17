/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SafeFileSorter implements FileSorter {

  @Override
  public List<File> sort(List<File> unsorted) {
    List<SortEntry> wrapped = wrap(unsorted);
    Collections.sort(wrapped);
    return unwrap(wrapped);
  }


  private List<SortEntry> wrap(List<File> unsorted) {
    List<SortEntry> result = new ArrayList<>(unsorted.size());
    for (File f : unsorted) {
      result.add(wrap(f));
    }
    return result;
  }

  private List<File> unwrap(List<SortEntry> sorted) {
    List<File> result = new ArrayList<>(sorted.size());
    for (SortEntry w : sorted) {
      result.add(w.getFile());
    }
    return result;
  }

  abstract SortEntry wrap(File f);

  abstract class SortEntry<T extends SortEntry<T>> implements Comparable<T> {
    private File file;

    SortEntry(File f) {
      file = f;
    }

    File getFile() {
      return file;
    }
  };

}
