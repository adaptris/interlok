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
