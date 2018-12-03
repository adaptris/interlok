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

package com.adaptris.core.lms;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;

import com.adaptris.core.fs.OlderThan;

public class FilePurge {
  private FileFilter fileFilter;

  private static final FilePurge purger = new FilePurge();
  private static final String PURGE_FILTER_NAME = "^" + FileBackedMessageFactory.TMP_FILE_PREFIX + ".*";
  private static final String PURGE_FILTER_AGE = "-PT30S";

  private FilePurge() {
    fileFilter = new TemporaryFileFilter();
  }

  public static FilePurge getInstance() {
    return purger;
  }

  public void purge() {
    try {
      File dir = new File(System.getProperty("java.io.tmpdir"));
      File[] files = dir.listFiles(fileFilter);
      for (File file : files) {
        FileUtils.deleteQuietly(file);
      }
    }
    catch (Exception ignoredIntentionally) {
      ;
    }
  }

  private class TemporaryFileFilter implements FileFilter {
    private List<FileFilter> filters = Arrays.asList(new RegexFileFilter(PURGE_FILTER_NAME), new OlderThan(PURGE_FILTER_AGE));

    @Override
    public boolean accept(File pathname) {
      int result = 0;
      for (FileFilter f : filters) {
        result += f.accept(pathname) ? 1 : 0;
      }
      return result == filters.size();
    }

  }
}
