package com.adaptris.core.fs;

import static com.adaptris.core.CoreConstants.FILE_LAST_MODIFIED_KEY;
import static com.adaptris.core.CoreConstants.FS_CONSUME_DIRECTORY;
import static com.adaptris.core.CoreConstants.FS_CONSUME_PARENT_DIR;
import static com.adaptris.core.CoreConstants.FS_FILE_SIZE;
import static com.adaptris.core.CoreConstants.ORIGINAL_NAME_KEY;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.enhanced.FileSorter;
import com.adaptris.core.fs.enhanced.NoSorting;
import com.adaptris.fs.FsException;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

/**
 * <p>
 * Abstract implementation of <code>AdaptrisMessageConsumer</code> based on the <code>com.adaptris.fs</code> package.
 * </p>
 */
public abstract class FsConsumerImpl extends AdaptrisPollingConsumer {

  private static final TimeInterval DEFAULT_OLDER_THAN = new TimeInterval(0L, TimeUnit.MILLISECONDS);
  private static final String DEFAULT_FILE_FILTER_IMP = "org.apache.oro.io.Perl5FilenameFilter";
  // marshalled
  private String fileFilterImp;
  private Boolean createDirs;
  private Boolean logAllExceptions;
  private TimeInterval quietInterval;
  @NotNull
  @Valid
  @AutoPopulated
  private FileSorter fileSorter;

  // not marshalled
  protected transient FileFilter fileFilter;
  protected transient FsWorker fsWorker = new NioWorker();

  public FsConsumerImpl() {
    setFileSorter(new NoSorting());
  }

  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Basic);
  }

  /**
   * <p>
   * If reacquire-lock-between-messages is set to true, this.reaquireLock is called after each message has been processed. This
   * gives other Threads (e.g. something stopping the adapter) the opportunity to obtain the lock without waiting for all messages
   * to be processed.
   * </p>
   *
   * @see com.adaptris.core.AdaptrisPollingConsumer#processMessages()
   */
  @Override
  protected int processMessages() {
    List<File> fileList;
    int filesProcessed = 0;
    try {
      File dir = verifyDirectory();
      fileList = Arrays.asList(dir.listFiles(fileFilter));
    }
    catch (Exception e) {
      log.warn("Exception listing files in [" + getDestination().getDestination() + "], waiting for next scheduled poll");
      if (logAllExceptions()) {
        log.warn(e.getMessage(), e);
      }
      return 0;
    }
    getFileSorter().sort(fileList);
    for (File file : fileList) {
      try {
        filesProcessed += processFile(file);
        if (!continueProcessingMessages()) {
          break;
        }
      }
      catch (Exception e) {
        log.warn("Exception processing [" + file.getName() + "], waiting for next scheduled poll");
        if (logAllExceptions()) {
          log.warn(e.getMessage(), e);
        }
      }
    }
    return filesProcessed;
  }

  /**
   * Does this file match the quiet period directive.
   *
   * @param f the file.
   * @return whether the file has been modified or not.
   * @throws IOException
   */
  protected boolean checkModified(File f) throws IOException {
    long mtimeCheck = olderThanMs();
    if (mtimeCheck > 0) {
      long now = System.currentTimeMillis();
      if (!(now - f.lastModified() >= mtimeCheck)) {
        log.trace("[" + f.getCanonicalPath() + "] not deemed safe to process, " + "lastModified=[" + new Date(f.lastModified())
            + "];file must be older than=[" + new Date(now - mtimeCheck) + "]");
        return false;
      }
    }
    return true;
  }

  /**
   * Could we read and process this file.
   *
   * @param f the file.
   * @return true, if the file is a file, can be read, and can be written to (for renaming purposes).
   */
  protected boolean isFileAccessible(File f) {
    return f.isFile() && f.canRead() && f.canWrite();
  }

  /**
   * Attempt to process this file which might be a directory.
   *
   * @param f the File
   * @return the number of files processed.
   * @throws CoreException wrapping any other Exception.
   */
  @Profiled(tag = "{$this.getClass().getSimpleName()}.processFile()", logger = "com.adaptris.perf4j.fs.TimingLogger")
  protected abstract int processFile(File f) throws CoreException;

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    try {
      verifyDirectory();
      initFileFilter();
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    super.init();
  }

  protected File verifyDirectory() throws Exception {
    File f = FsHelper.createFileReference(FsHelper.createUrlFromString(getDestination().getDestination(), true));
    if (shouldCreateDirs()) {
      if (!f.exists()) {
        log.trace("Creating non-existent directory " + f.getCanonicalPath());
        f.mkdirs();
      }
    }
    if (!fsWorker.isWriteableDir(f)) {
      throw new Exception("please check that [" + f.getCanonicalPath() + "] exists and is writeable");
    }
    return f;
  }

  /**
   * Specify whether to create directories that do not exist.
   * <p>
   * When the ConsumeDestination returns a destination, if this flag has been set, then an attempt to create the directory is made,
   * if the directory does not exist.
   *
   * @param b true to enable directory creation; default false.
   */
  public void setCreateDirs(Boolean b) {
    createDirs = b;
  }

  /**
   * Get the flag specifying creation of directories.
   *
   * @return true or false.
   */
  public Boolean getCreateDirs() {
    return createDirs;
  }

  public boolean shouldCreateDirs() {
    return getCreateDirs() != null ? getCreateDirs().booleanValue() : false;
  }

  protected AdaptrisMessage createAdaptrisMessage(File fileToProcess) throws CoreException {
    AdaptrisMessage msg = null;
    try {
      long lastModified = fileToProcess.lastModified();
      msg = decode(fsWorker.get(fileToProcess));
    }
    catch (FsException e) {
      throw new CoreException(e);
    }
    return msg;
  }

  protected void addStandardMetadata(AdaptrisMessage msg, File originalFile, File wipFile) throws CoreException {
    if (originalFile == null) {
      return;
    }
    try {
      long lastModified = wipFile.lastModified();
      msg.addMetadata(ORIGINAL_NAME_KEY, originalFile.getName());
      msg.addMetadata(FILE_LAST_MODIFIED_KEY, "" + lastModified);
      msg.addMetadata(FS_FILE_SIZE, "" + wipFile.length());
      File parent = originalFile.getParentFile();
      if (parent != null) {
        msg.addMetadata(FS_CONSUME_DIRECTORY, parent.toURI().toURL().getFile());
        msg.addMetadata(FS_CONSUME_PARENT_DIR, parent.getName());
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   * <p>
   * NB the <code>String</code> expression that is used to filter messages is obtained from <code>ConsumeDestination</code>.
   * </p>
   */
  private void initFileFilter() throws Exception {
    String filterExp = getDestination().getFilterExpression();

    if (filterExp != null) {
      Class[] paramTypes =
      {
        filterExp.getClass()
      };
      Object[] args =
      {
        filterExp
      };

      Class c = Class.forName(fileFilterImp());
      Constructor cnst = c.getDeclaredConstructor(paramTypes);

      fileFilter = (FileFilter) cnst.newInstance(args);
    }
  }


  // accessors...


  /**
   * Set the filename filter
   * 
   * @param string the classname of the {@link FileFilter} implementation to use, if not specified, then it defaults to
   *          "org.apache.oro.io.Perl5FilenameFilter" which uses the jakarta oro package implementation of a Perl regular expression
   *          filename filter.
   * @see ConsumeDestination#getFilterExpression()
   */
  public void setFileFilterImp(String string) {
    fileFilterImp = string;
  }

  /**
   * <p>
   * Returns the name of the <code>FileFilter</code> being used.
   * </p>
   *
   * @return the name of the <code>FileFilter</code> being used
   */
  public String getFileFilterImp() {
    return fileFilterImp;
  }

  String fileFilterImp() {
    return getFileFilterImp() != null ? getFileFilterImp() : DEFAULT_FILE_FILTER_IMP;
  }

  /**
   * @return the logAllExceptions
   */
  public Boolean getLogAllExceptions() {
    return logAllExceptions;
  }

  /**
   * Whether or not to log all stacktraces.
   *
   * @param b the logAllExceptions to set, default true
   */
  public void setLogAllExceptions(Boolean b) {
    logAllExceptions = b;
  }

  public boolean logAllExceptions() {
    return getLogAllExceptions() != null ? getLogAllExceptions().booleanValue() : true;
  }

  long olderThanMs() {
    return getQuietInterval() != null ? getQuietInterval().toMilliseconds() : DEFAULT_OLDER_THAN.toMilliseconds();
  }

  public TimeInterval getQuietInterval() {
    return quietInterval;
  }

  /**
   * Specify how old a file must be before a file is deemed safe to be processed.
   * <p>
   * The purpose of this is to delay processing of files that may be currently being written to by another process. On certain
   * platforms (e.g. most Unix) it is still possible to obtain an exclusive lock on the file even though it is being written to by
   * another process.
   * </p>
   * <p>
   * An alternative to specifying a last-modified is to specify {@link CompositeFileFilter} as the filter implementation and then a
   * combination of {@link OlderThan} along with your actual filter-implementation.
   * </p>
   * <p>
   * <strong>Note: your mileage may vary when using this setting. The only surefire way is for the triggering application to write
   * the file to a staging area and use an atomic operation (such as move) to move the file into the target directory.</strong>
   * </p>
   *
   * @param interval the interval to set (default is 0)
   * @see File#lastModified()
   * @see CompositeFileFilter
   * @see #setFileFilterImp(String)
   */
  public void setQuietInterval(TimeInterval interval) {
    quietInterval = interval;
  }

  public FileSorter getFileSorter() {
    return fileSorter;
  }

  /**
   * Set the filesorter implementation to use.
   * <p>
   * The file sorter is responsible for sorting the list of files that is collected for processing. The sorted list is then
   * processed.
   * </p>
   *
   * @param fs the sorter, default is {@link NoSorting}
   */
  public void setFileSorter(FileSorter fs) {
    if (fs == null) {
      throw new IllegalArgumentException("File Sorter implementation is null");
    }
    fileSorter = fs;
  }


}
