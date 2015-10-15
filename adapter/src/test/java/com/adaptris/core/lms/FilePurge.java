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
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.fs.CompositeFileFilter;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class FilePurge {
  private FileFilter fileFilter;
  private Calendar nextRun = null;
  private transient Log logR = LogFactory.getLog(this.getClass());

  private static final FilePurge purger = new FilePurge();
  private static final String PURGE_FILTER = "Glob=" + FileBackedMessageFactory.TMP_FILE_PREFIX + "*"
      + FileBackedMessageFactory.TMP_FILE_SUFFIX + "__@@__" + "OlderThan=-PT30S";

  private FilePurge() {
    fileFilter = new CompositeFileFilter(PURGE_FILTER, true);
    nextRun = Calendar.getInstance();
  }

  public static FilePurge getInstance() {
    return purger;
  }

  public void purge() {
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("FilePurge");
      File dir = new File(System.getProperty("java.io.tmpdir"));
      File[] files = dir.listFiles(fileFilter);
      for (File file : files) {
        try {
          if (file.exists()) {
            logR.trace("Deleting " + file.getCanonicalPath());
            file.delete();
          }
        }
        catch (Exception e) {
          ;
        }
      }
    }
    catch (Exception ignoredIntentionally) {
      ;
    }
    Thread.currentThread().setName(oldName);
  }

}
