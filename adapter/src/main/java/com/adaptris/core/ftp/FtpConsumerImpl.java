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

import static com.adaptris.core.ftp.FtpHelper.FORWARD_SLASH;
import static com.adaptris.core.ftp.FtpHelper.getUnqualifiedFilename;

import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.util.TimeInterval;

/**
 * Abstract FTP Implementation of the {@link AdaptrisMessageConsumer} implementation.
 */
public abstract class FtpConsumerImpl extends AdaptrisPollingConsumer {
  protected static final TimeInterval DEFAULT_OLDER_THAN = new TimeInterval(0L, TimeUnit.MILLISECONDS);
  protected static final String DEFAULT_FILE_FILTER_IMPL = "org.apache.oro.io.GlobFilenameFilter";

  @AdvancedConfig
  private String fileFilterImp;
  @AdvancedConfig
  private TimeInterval quietInterval;

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
      fileFilter = FsHelper.createFilter(getDestination().getFilterExpression(), fileFilterImp());
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

  protected AdaptrisMessage addStandardMetadata(AdaptrisMessage msg, String filename) {
    msg.addMetadata(CoreConstants.ORIGINAL_NAME_KEY, filename);
    msg.addMetadata(CoreConstants.FS_FILE_SIZE, "" + msg.getSize());
    return msg;
  }

  protected String getFilename(String fullPath) {
    String result = fullPath;
    int pos = fullPath.lastIndexOf(FORWARD_SLASH);
    if (pos >= 0 && fullPath.length() > pos) {
      result = fullPath.substring(pos + 1);
    }
    return result;
  }

  @Override
  protected int processMessages() {
    int count = 0;
    String pollDirectory;
    FileTransferConnection con = retrieveConnection(FileTransferConnection.class);
    try {
      ftpClient = con.connect(getDestination().getDestination());
      pollDirectory = configureWorkDir(con.getDirectoryRoot(getDestination().getDestination()));
    }
    catch (Exception e) {
      log.error("Failed to connect to [{}]", getDestination().getDestination(), e);
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
        String fileToGet = pollDirectory + FORWARD_SLASH + getUnqualifiedFilename(file, con.windowsWorkaround());
        count += handle(fileToGet) ? 1 : 0;
        if (!continueProcessingMessages()) {
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

  protected String fileFilterImp() {
    return getFileFilterImp() != null ? getFileFilterImp() : DEFAULT_FILE_FILTER_IMPL;
  }

  /**
   * @return Returns the fileFilterImp.
   */
  public String getFileFilterImp() {
    return fileFilterImp;
  }

  /**
   * Set the filename filter implementation that will be used for filtering files.
   * <p>
   * The <code>String</code> expression that is used to filter messages is obtained from <code>ConsumeDestination</code>.
   * </p>
   * <p>
   * Note that because we working against a remote server, support for additional file attributes such as size (e.g. via
   * {@link com.adaptris.core.fs.SizeGreaterThan}) or last modified may not be supported. We encourage you to stick with filtering
   * by filename only.
   * </p>
   * 
   * @param s The fileFilterImp to set, if not specified, then the default is "org.apache.oro.io.GlobFilenameFilter" which uses the
   *          jakarta oro package to perform unix glob style filtering
   * @see com.adaptris.core.ConsumeDestination#getFilterExpression()
   */
  public void setFileFilterImp(String s) {
    fileFilterImp = s;
  }

  protected long olderThanMs() {
    return getQuietInterval() != null ? getQuietInterval().toMilliseconds() : DEFAULT_OLDER_THAN.toMilliseconds();
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

}
