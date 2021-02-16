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

package com.adaptris.core.fs;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.enhanced.FileSorter;
import com.adaptris.core.fs.enhanced.NoSorting;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.fs.FsException;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.adaptris.interlok.util.FileFilterBuilder;
import com.adaptris.util.TimeInterval;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import javax.management.MalformedObjectNameException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.adaptris.core.CoreConstants.FILE_LAST_MODIFIED_KEY;
import static com.adaptris.core.CoreConstants.FS_CONSUME_DIRECTORY;
import static com.adaptris.core.CoreConstants.FS_CONSUME_PARENT_DIR;
import static com.adaptris.core.CoreConstants.FS_FILE_SIZE;
import static com.adaptris.core.CoreConstants.ORIGINAL_NAME_KEY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * <p>
 * Abstract implementation of {@link com.adaptris.core.AdaptrisMessageConsumer} based on the <code>com.adaptris.fs</code> package.
 * </p>
 */
public abstract class FsConsumerImpl extends AdaptrisPollingConsumer {

  private static final TimeInterval DEFAULT_OLDER_THAN = new TimeInterval(0L, TimeUnit.MILLISECONDS);

  /**
   * Set the filename filter implementation that will be used for filtering files.
   * <p>
   * The file filter implementation that is used in conjunction with the
   * {@link #getFilterExpression()}, if not specified, then the default is
   * {@code org.apache.commons.io.filefilter.RegexFileFilter} which uses the java.util regular
   * expressions to perform filtering
   * </p>
   * <p>
   * The expression that is used to filter messages is derived from {@link #getFilterExpression()}.
   * </p>
   *
   * @see #getFilterExpression()
   */
  @InputFieldHint(ofType = "java.io.FileFilter")
  @AdvancedConfig
  @Getter
  @Setter
  private String fileFilterImp;
  /**
   * Create missing directories when trying to poll
   *
   * <p>
   * this defaults to false because we consider it a dangerous thing to do especially if you have
   * bad configuration. The last thing we want is for you to accuse interlok of creating random
   * directories.
   * </p>
   */
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean createDirs;
  /**
   * Log all the stack traces or not.
   *
   */
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean logAllExceptions;
  /**
   * Specify how old a file must be before a file is deemed safe to be processed.
   * <p>
   * The purpose of this is to delay processing of files that may be currently being written to by
   * another process. On certain platforms (e.g. most Unix) it is still possible to obtain an
   * exclusive lock on the file even though it is being written to by another process.
   * </p>
   * <p>
   * An alternative to specifying a last-modified is to specify {@link CompositeFileFilter} as the
   * filter implementation and then a combination of {@link OlderThan} along with your actual
   * filter-implementation.
   * </p>
   * <p>
   * <strong>Note: your mileage may vary when using this setting. The only surefire way is for the
   * triggering application to write the file to a staging area and use an atomic operation (such as
   * move) to move the file into the target directory.</strong>
   * </p>
   *
   * @see File#lastModified()
   * @see CompositeFileFilter
   * @see #setFileFilterImp(String)
   */
  @AdvancedConfig
  @Getter
  @Setter
  private TimeInterval quietInterval;

  /**
   * Set the filesorter implementation to use.
   * <p>
   * The file sorter is responsible for sorting the list of files that is collected for processing.
   * The sorted list is then processed.
   * </p>
   *
   */
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  @Getter
  @Setter
  @InputFieldDefault(value = "no-sorting")
  @NonNull
  private FileSorter fileSorter;

  /**
   * The base directory specified as a URL.
   *
   */
  @Getter
  @Setter
  @NotBlank
  private String baseDirectoryUrl;

  /**
   * The filter expression to use when listing files.
   * <p>
   * If not specified then will default in a file filter that matches all files.
   * </p>
   */
  @Getter
  @Setter
  private String filterExpression;


  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  // not marshalled
  protected transient FileFilter fileFilter;
  protected transient FsWorker fsWorker = new NioWorker();
  private transient boolean destinationWarningLogged = false;

  public FsConsumerImpl() {
    setFileSorter(new NoSorting());
  }

  @Override
  protected void prepareConsumer() throws CoreException {

  }

  protected String baseDirUrl() {
    return getBaseDirectoryUrl();
  }

  protected String filterExpression() {
    return getFilterExpression();
  }

  @Override
  protected String newThreadName() {
    return retrieveAdaptrisMessageListener().friendlyName();
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
      log.warn("Exception listing files in [{}], waiting for next scheduled poll", baseDirUrl());
      if (logAllExceptions()) {
        log.trace(e.getMessage(), e);
      }
      return 0;
    }
    fileList = getFileSorter().sort(fileList);
    for (File file : fileList) {
      try {
        filesProcessed += processFile(file);
        if (!continueProcessingMessages(filesProcessed)) {
          break;
        }
      }
      catch (Exception e) {
        log.warn("Exception processing [{}], waiting for next scheduled poll", file.getName());
        if (logAllExceptions()) {
          log.trace(e.getMessage(), e);
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
        log.trace("[{}] not safe to process: lastModified=[{}]; must be older than [{}]", f.getCanonicalPath(),
            new Date(f.lastModified()), new Date(now - mtimeCheck));
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
  protected abstract int processFile(File f) throws CoreException;

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    try {
      verifyDirectory();
      fileFilter = FsHelper.createFilter(filterExpression(), fileFilterImp());
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    super.init();
  }


  protected File verifyDirectory() throws Exception {
    File f = FsHelper.createFileReference(FsHelper.createUrlFromString(baseDirUrl(), true));
    if (shouldCreateDirs()) {
      if (!f.exists()) {
        log.trace("Creating non-existent directory {}", f.getCanonicalPath());
        f.mkdirs();
      }
    }
    if (!fsWorker.isWriteableDir(f)) {
      throw new Exception(
          "please check that [" + f.getCanonicalPath() + "] exists and is writeable");
    }
    return f;
  }

  public boolean shouldCreateDirs() {
    return BooleanUtils.toBooleanDefaultIfNull(getCreateDirs(), false);
  }

  protected AdaptrisMessage createAdaptrisMessage(File fileToProcess) throws CoreException {
    AdaptrisMessage msg = null;
    try {
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

  String fileFilterImp() {
    return ObjectUtils.defaultIfNull(getFileFilterImp(), FileFilterBuilder.DEFAULT_FILE_FILTER_IMP);
  }

  public boolean logAllExceptions() {
    return BooleanUtils.toBooleanDefaultIfNull(getLogAllExceptions(), true);
  }

  long olderThanMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getQuietInterval(), DEFAULT_OLDER_THAN);
  }

  int filesRemaining() throws Exception {
    return verifyDirectory()
        .listFiles(FsHelper.createFilter(filterExpression(), fileFilterImp())).length;
  }

  /**
   * Provides the metadata key '{@value com.adaptris.core.CoreConstants#FS_CONSUME_DIRECTORY}' that
   * contains the directory (if not null) where the file was read from.
   *
   * @since 3.9.0
   */
  @Override
  public String consumeLocationKey() {
    return FS_CONSUME_DIRECTORY;
  }

  public <T extends FsConsumerImpl> T withBaseDirectoryUrl(String s) {
    setBaseDirectoryUrl(s);
    return (T) this;
  }

  public <T extends FsConsumerImpl> T withFilterExpression(String s) {
    setFilterExpression(s);
    return (T) this;
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof FsConsumerImpl) {
        return !isEmpty(((FsConsumerImpl) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new FsConsumerMonitor((WorkflowManager) parent, (FsConsumerImpl) e);
    }

  }

}
