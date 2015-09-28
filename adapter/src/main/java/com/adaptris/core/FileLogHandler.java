package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.management.MalformedObjectNameException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Zip file based implemention of <code>LogHandler</code>. This LogHandler returns the specified log file (up to maximum of 5Mb)
 * compressed using the GZIP algorithm.
 * </p>
 * 
 * @config file-log-handler
 * 
 * @license BASIC
 */
@XStreamAlias("file-log-handler")
@GenerateBeanInfo
public class FileLogHandler extends LogHandlerImp {

  private static final long LOG_FILE_MAX_SIZE = 5 * 1024 * 1024;

  private int period;
  private transient Map<LogFileType, String> logFiles;

  private String logDirectory;
  private String logFile;
  @Deprecated
  private String statisticsLogFile;
  @Deprecated
  private String statisticsGraphLogFile;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());
  @Deprecated
  private Boolean useCompression;
  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  public FileLogHandler() {
    period = 30;
    logFiles = new HashMap<LogFileType, String>();
  }

  /**
   *
   * @see com.adaptris.core.LogHandler#isCompressed()
   */
  @Override
  public boolean isCompressed() {
    return getUseCompression() != null ? getUseCompression().booleanValue() : true;
  }

  @Override
  public InputStream retrieveLog(LogFileType type) throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    OutputStream output = byteOut;
    Reader input = null;
    try {
      if (isCompressed()) {
        output = new GZIPOutputStream(byteOut);
      }
      if (exists(type)) {
        String logFile = logFiles.get(type);
        File file = new File(logDirectory, logFile);
        input = new FileReader(file);
        long size = LOG_FILE_MAX_SIZE;
        if (file.length() <= LOG_FILE_MAX_SIZE) {
          size = file.length();
        }
        else {
          input.skip(file.length() - LOG_FILE_MAX_SIZE);
        }
        IOUtils.copy(input, output, "UTF-8");
        output.flush();
      }
      else {
        PrintStream printer = new PrintStream(output, true, "UTF-8");
        printer.println("Log file type " + type.name() + " not found, try requesting manually");
        printer.flush();
      }
      if (isCompressed()) {
        ((GZIPOutputStream) output).finish();
      }
    }
    finally {
      IOUtils.closeQuietly(input);
      IOUtils.closeQuietly(output);
      IOUtils.closeQuietly(byteOut);
    }
    return new ByteArrayInputStream(byteOut.toByteArray());
  }

  private boolean exists(LogFileType type) {
    String actualLogfile = logFiles.get(type);
    if (logDirectory == null || isBlank(actualLogfile)) {
      return false;
    }
    File f = new File(logDirectory, actualLogfile);
    return f.exists() && f.length() > 0;
  }

  /**
   * This deletes any files in the log directory older than the clean period.
   *
   * @see com.adaptris.core.LogHandler#clean()
   */
  @Override
  public void clean() throws IOException {
    for (LogFileType type : LogFileType.values()) {
      String root = logFiles.get(type);
      if (!isBlank(root)) {
        FileCleaner maid = new FileCleaner(new File(logDirectory), logFiles.get(type), 0 - period);
        maid.clean();
      }
    }
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
    if (isBlank(s)) {
      throw new IllegalArgumentException("Empty Log file");
    }
    logFiles.put(LogFileType.Standard, s);
    logFile = s;
  }

  /**
   * Return the filename that will be sent as part of the LogRequestEvent.
   *
   * @return the configured logfile.
   */
  public String getLogFile() {
    return logFiles.get(LogFileType.Standard);
  }

  /**
   * Set the cleaning period. Must be at least one day.
   *
   * @param days the cleaning period in days.
   */
  public void setPeriod(int days) {
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
  public int getPeriod() {
    return period;
  }

  /**
   * Get the compression flag.
   *
   * @return true if compression is currently enabled.
   * @deprecated since 3.0.6 this has no meaning as {@link FileLogHandler} never returns you the logfile.
   */
  @Deprecated
  public Boolean getUseCompression() {
    return useCompression;
  }

  /**
   * Force the file to be returned compressed.
   *
   * @param b true to use compression.
   * @deprecated since 3.0.6 this has no meaning as {@link FileLogHandler} never returns you the logfile.
   */
  @Deprecated
  public void setUseCompression(Boolean b) {
    useCompression = b;
  }

  /**
   * 
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  public String getStatisticsLogFile() {
    return logFiles.get(LogFileType.Statistics);
  }

  /**
   * 
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  public void setStatisticsLogFile(String s) {
    if (isBlank(s)) {
      throw new IllegalArgumentException("Empty Statistics Log file");
    }
    logFiles.put(LogFileType.Statistics, s);
    statisticsLogFile = s;

  }

  /**
   * 
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  public String getStatisticsGraphLogFile() {
    return logFiles.get(LogFileType.Graphing);
  }

  /**
   * 
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  public void setStatisticsGraphLogFile(String s) {
    if (isBlank(s)) {
      throw new IllegalArgumentException("Empty Statistics Graphing Log file");
    }
    logFiles.put(LogFileType.Graphing, s);
    statisticsGraphLogFile = s;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
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

}
