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

package com.adaptris.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import javax.management.MalformedObjectNameException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.util.Args;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Zip file based implemention of <code>LogHandler</code>. This LogHandler returns the specified log file (up to maximum of 5Mb)
 * compressed using the GZIP algorithm.
 * </p>
 * 
 * @config file-log-handler
 * 
 * 
 */
@XStreamAlias("file-log-handler")
public class FileLogHandler extends LogHandlerImp {

  private static final int DEFAULT_PERIOD = 30;

  private Integer period;
  private String logDirectory;
  private String logFile;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());
  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  public FileLogHandler() {
  }

  /**
   * This deletes any files in the log directory older than the clean period.
   *
   * @see com.adaptris.core.LogHandler#clean()
   */
  @Override
  public void clean() throws IOException {
    FileCleaner maid = new FileCleaner(new File(logDirectory), getLogFile(), 0 - period());
    maid.clean();
  }

  /**
   * Set the directory that is used for logging.
   *
   * @param dir the directory.
   */
  public void setLogDirectory(String dir) {
    logDirectory = dir;
  }

  /**
   * Get the directory used for logging.
   *
   * @return the directory.
   */
  public String getLogDirectory() {
    return logDirectory;
  }

  /**
   * The filename that will be sent as part of any LogRequestEvent.
   *
   * @param s the file.
   */
  public void setLogFile(String s) {
    logFile = Args.notBlank(s, "logFile");
  }

  /**
   * Return the filename that will be sent as part of the LogRequestEvent.
   *
   * @return the configured logfile.
   */
  public String getLogFile() {
    return logFile;
  }

  /**
   * Set the cleaning period. Must be at least one day.
   *
   * @param days the cleaning period in days.
   */
  public void setPeriod(Integer days) {
    if (days <= 0) {
      throw new IllegalArgumentException("cleaning period must be at least one day");
    }
    period = days;
  }

  /**
   * Get the clean period.
   *
   * @return the clean period in days.
   */
  public Integer getPeriod() {
    return period;
  }

  int period() {
    return NumberUtils.toIntDefaultIfNull(getPeriod(), DEFAULT_PERIOD);
  }

  /**
   * <p>
   * Comment required...
   * </p>
   */
  private class FileCleaner implements FilenameFilter {
    private String fileRoot;
    private File parent;
    private int interval;

    FileCleaner(File file, String root, int period) {
      parent = file;
      fileRoot = root;
      interval = period;
    }

    void clean() {
      File[] files = filterFiles(parent.listFiles(this));
      for (int i = 0; i < files.length; i++) {
        FileUtils.deleteQuietly(files[i]);
      }
    }

    private File[] filterFiles(File[] f) {
      ArrayList<File> list = new ArrayList<File>();
      if (f != null) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, interval);
        long limit = cal.getTime().getTime();
        for (int i = 0; i < f.length; i++) {
          if (f[i].lastModified() < limit) {
            list.add(f[i]);
          }
        }
      }
      return list.toArray(new File[0]);
    }

    /**
     * @see FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept(File file, String s) {
      return s.startsWith(fileRoot);
    }
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof FileLogHandler) {
        return true;
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new FileLogHandlerJmx((AdapterManager) parent, (FileLogHandler) e);
    }

  }

  @Override
  public void prepare() throws CoreException {
  }

}
