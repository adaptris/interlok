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

package com.adaptris.core.ftp;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.interlok.util.FileFilterBuilder;
import com.adaptris.util.TimeInterval;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.constraints.NotBlank;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.adaptris.core.CoreConstants.FS_CONSUME_DIRECTORY;
import static com.adaptris.core.ftp.FtpHelper.FORWARD_SLASH;

/**
 * Abstract FTP Implementation of the {@link AdaptrisMessageConsumer} implementation.
 */
public abstract class FtpConsumerImpl extends AdaptrisPollingConsumer {
  protected static final TimeInterval DEFAULT_OLDER_THAN = new TimeInterval(0L, TimeUnit.MILLISECONDS);
  /**
   * @deprecated since 3.11.1 {@link FileFilterBuilder#DEFAULT_FILE_FILTER_IMP instead.
   *
   */
  @Deprecated
  protected static final String DEFAULT_FILE_FILTER_IMPL =
  FileFilterBuilder.DEFAULT_FILE_FILTER_IMP;

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
   * <p>
   * Note that because we working against a remote server, support for additional file attributes
   * such as size (e.g. via {@link com.adaptris.core.fs.SizeGreaterThan}) or last modified may not
   * be supported. We encourage you to stick with filtering by filename only.
   * </p>
   *
   * @see #getFilterExpression()
   */
  @InputFieldHint(ofType = "java.io.FileFilter")
  @AdvancedConfig
  @Getter
  @Setter
  private String fileFilterImp;
  @AdvancedConfig
  private TimeInterval quietInterval;

  /**
   * The FTP endpoint where we will retrieve files files.
   * <p>
   * Although nominally a URL, you can configure the following styles
   * <ul>
   * <li>Just the server name / IP Address (e.g. 10.0.0.1) in which case the username and password
   * from the corresponding {@link FileTransferConnection} will be used to supply the username and
   * password. You will be working off directly off the perceived root filesystem which will be a
   * problem if you aren't in a chroot jail.</li>
   * <li>A FTP style URL {@code ftp://10.0.0.1/path/to/dir}, the username and password will be taken
   * from the corresponding connection. The working directory start with {@code /path/to/dir}</li>
   * <li>A FTP style URL with a username/password {@code ftp://user:password@10.0.0.1/path/to/dir}.
   * The working directory will start with {@code /path/to/dir}</li>
   * </ul>
   */
  @Getter
  @Setter
  @NotBlank
  private String ftpEndpoint;

  /**
   * The filter expression to use when listing files.
   * <p>
   * If not specified then will default in a file filter that matches all files.
   * </p>
   */
  @Getter
  @Setter
  private String filterExpression;


  protected transient FileFilter fileFilter;
  protected transient FileTransferClient ftpClient = null;

  public FtpConsumerImpl() {
    setReacquireLockBetweenMessages(true);
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    try {
      fileFilter = FsHelper.createFilter(filterExpression(), fileFilterImp());
      super.init();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  protected boolean additionalDebug() {
    return retrieveConnection(FileTransferConnection.class).additionalDebug();
  }

  protected boolean oldEnough(String fullPath) throws FileTransferException, IOException {
    boolean result = true;
    long olderThanMs = olderThanMs();
    if (olderThanMs > 0) {
      long now = System.currentTimeMillis();
      long lastModified = ftpClient.lastModified(fullPath);
      if (additionalDebug()) {
        log.trace("lastModified for [{}] is [{}]", fullPath, new Date(lastModified));
      }
      if (!(now - lastModified >= olderThanMs)) {
        log.trace("[{}] not deemed safe to process, lastModified on server=[{}]; file must be older than=[{}]", fullPath,
            new Date(lastModified), new Date(now - olderThanMs));
        result = false;
      }
    }
    return result;
  }

  protected AdaptrisMessage addStandardMetadata(AdaptrisMessage msg, String filename, String dir) {
    msg.addMetadata(CoreConstants.ORIGINAL_NAME_KEY, filename);
    msg.addMetadata(CoreConstants.FS_CONSUME_DIRECTORY, dir);
    msg.addMetadata(CoreConstants.FS_FILE_SIZE, "" + msg.getSize());
    return msg;
  }

  @Override
  protected int processMessages() {
    int count = 0;
    String pollDirectory;
    FileTransferConnection con = retrieveConnection(FileTransferConnection.class);
    String hostUrl = ftpURL();
    try {
      ftpClient = con.connect(hostUrl);
      pollDirectory = configureWorkDir(con.getDirectoryRoot(hostUrl));
    }
    catch (Exception e) {
      log.error("Failed to connect to [{}]", hostUrl, e);
      return 0;
    }
    try {
      if (additionalDebug()) {
        log.trace("Polling [{}]", pollDirectory);
      }
      String[] files = ftpClient.dir(pollDirectory, fileFilter);
      if (additionalDebug()) {
        log.trace("There are potentially [{}] messages to process", files.length);
      }
      for (String file : files) {
        String fileToGet = pollDirectory + FORWARD_SLASH + FtpHelper.getFilename(file, con.windowsWorkaround());
        count += handle(fileToGet) ? 1 : 0;
        if (!continueProcessingMessages(count)) {
          break;
        }
      }
    }
    catch (Exception e) {
      log.warn("Failed to poll [{}] hoping for success next poll time", pollDirectory);
      if (additionalDebug()) {
        log.trace("Exception was : {}", e.getMessage(), e);
      }
    }
    finally {
      con.disconnect(ftpClient);
      ftpClient = null;
    }
    return count;
  }

  private boolean handle(String fileToGet) {
    try {
      if (accept(fileToGet)) {
        return fetchAndProcess(fileToGet);
      }
    }
    catch (Exception e) {
      log.error("Error processing [{}]", fileToGet, e);
    }
    return false;
  }

  protected abstract boolean fetchAndProcess(String fullPath) throws Exception;

  protected String configureWorkDir(String path) {
    return path;
  }

  protected boolean accept(String path) throws Exception {
    return oldEnough(path);
  }

  @Override
  protected void prepareConsumer() throws CoreException {

  }

  protected String ftpURL() {
    return getFtpEndpoint();
  }

  protected String filterExpression() {
    return getFilterExpression();
  }

  protected String fileFilterImp() {
    return ObjectUtils.defaultIfNull(getFileFilterImp(), FileFilterBuilder.DEFAULT_FILE_FILTER_IMP);
  }

  protected long olderThanMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getQuietInterval(), DEFAULT_OLDER_THAN);
  }

  public TimeInterval getQuietInterval() {
    return quietInterval;
  }

  /**
   * Specify the time in which a file has been untouched before it is deemed safe to be processed.
   * <p>
   * The purpose of this is to delay processing of files that may be currently being written to by another process. On certain
   * platforms (e.g. most Unix) it is still possible to obtain an exclusive lock on the file even though it is being written to by
   * another process.
   * </p>
   * <p>
   * <strong>Note: your mileage may vary when using this setting. The FTP Server and the FTP Client will almost certainly have to
   * time-synchronized. Depending on the FTP Server implementation in question, you may need to additionally specify the server's
   * timezone in order to get accurate information.</strong>Additionally, the remote FTP server needs to support support the MDTM
   * command.
   * </p>
   *
   * @param interval the quietPeriod to set (default to 0)
   * @see FtpConnection#setServerTimezone(String)
   * @see com.adaptris.core.fs.CompositeFileFilter
   * @see #setFileFilterImp(String)
   */
  public void setQuietInterval(TimeInterval interval) {
    quietInterval = interval;
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
}
