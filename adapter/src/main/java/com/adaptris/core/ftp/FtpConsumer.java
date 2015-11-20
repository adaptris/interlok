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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.File;
import java.io.FileFilter;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * FTP implementation of the AdaptrisMessageConsumer interface.
 * <p>
 * The connection type for this consumer should always be a concrete implementation of {@link FileTransferConnection}.
 * </p>
 * <p>
 * The destination returned by the ConsumeDestination implementation should be in the form in the URL form dictated by the
 * <code>FileTransferConnection</code> flavour or simply the IP Address / DNS name of the target Server. If the URL form is used,
 * then it is possible to override the username, password, and port settings of the server, in all other cases the configuration
 * specified in the <code>FileTransferConnection</code> object will be used.
 * </p>
 * <p>
 * In the event the proc-directory is not configured, then after processing the file, it is deleted. If proc-directory is
 * configured, then the remote file will be renamed to this directory
 * </p>
 * <p>
 * The configuration of this consumer closely mirrors that of the FsConsumer though it does not, at the moment, share any common
 * hierarchy.
 * </p>
 * 
 * @config ftp-consumer
 * 
 * @see FtpConnection
 * @see SftpConnection
 * @see FileTransferConnection
 * @see com.adaptris.core.ConsumeDestination
 * @author lchan
 */
@XStreamAlias("ftp-consumer")
public class FtpConsumer extends AdaptrisPollingConsumer {
  private static final TimeInterval DEFAULT_OLDER_THAN = new TimeInterval(0L, TimeUnit.MILLISECONDS);
  private static final String DEFAULT_WIP_SUFFIX = "_wip";
  private static final String DEFAULT_FILE_FILTER_IMPL = "org.apache.oro.io.GlobFilenameFilter";

  private static final String FORWARD_SLASH = "/";
  private static final String BACK_SLASH = "\\";

  @NotNull
  @AutoPopulated
  private String workDirectory = "/work";
  @AdvancedConfig
  private String procDirectory;
  @AdvancedConfig
  private String fileFilterImp;
  @AdvancedConfig
  private String wipSuffix;
  @AdvancedConfig
  private TimeInterval quietInterval;

  private transient FileFilter fileFilter;
  private transient boolean additionalDebug = false;
  private transient FileTransferClient ftpClient = null;

  public FtpConsumer() {
    setReacquireLockBetweenMessages(true);
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    initFileFilter();
    if (workDirectory == null) {
      throw new CoreException("No Directory specified to read from");
    }
    if (!workDirectory.startsWith(FORWARD_SLASH)) {
      workDirectory = FORWARD_SLASH + workDirectory;
    }
    if (procDirectory != null && !procDirectory.startsWith(FORWARD_SLASH)) {
      procDirectory = FORWARD_SLASH + procDirectory;
    }
    super.init();
  }

  /**
   * <p>
   * NB the <code>String</code> expression that is used to filter messages is obtained from <code>ConsumeDestination</code>.
   * </p>
   */
  private void initFileFilter() throws CoreException {
    String filterExp = getDestination().getFilterExpression();
    try {
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
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisPollingConsumer#processMessages()
   */
  @Override
  protected int processMessages() {
    int count = 0;
    String pollDirectory;
    String procDir = null;
    FileTransferConnection con = retrieveConnection(FileTransferConnection.class);
    additionalDebug = con.additionalDebug();
    try {
      ftpClient = con.connect(getDestination().getDestination());
      pollDirectory = con.getDirectoryRoot(getDestination().getDestination()) + workDirectory;
      if (procDirectory != null) {
        procDir = con.getDirectoryRoot(getDestination().getDestination()) + procDirectory;
      }
    }
    catch (Exception e) {
      log.error("Failed to connect to " + getDestination().getDestination(), e);
      return 0;
    }
    try {
      if (additionalDebug) {
        log.trace("Polling " + pollDirectory);
      }
      String[] files = null;
      if (fileFilter != null) {
        files = ftpClient.dir(pollDirectory, fileFilter);
      }
      else {
        files = ftpClient.dir(pollDirectory);
      }
      if (additionalDebug) {
        log.trace("There are potentially [" + files.length + "] messages to process");
      }
      for (int i = 0; i < files.length; i++) {
        try {
          if (files[i].endsWith(wipSuffix())) {
            log.warn("[" + files[i] + "] matches [" + wipSuffix() + "], assuming part processed and ignoring");
          }
          else {
            count += processMessage(pollDirectory + FORWARD_SLASH + getUnqualifiedFilename(files[i]), procDir) ? 1 : 0;
          }
        }
        catch (Exception e) {
          log.error("Error processing " + pollDirectory + FORWARD_SLASH + files[i] + " from remote host", e);
        }
        if (!continueProcessingMessages()) {
          break;
        }
      }
    }
    catch (Exception e) {
      log.warn("Failed to poll [" + pollDirectory + "] hoping for success next poll time");
      if (additionalDebug) {
        log.trace("Exception was : " + e.getMessage(), e);
      }
    }
    finally {
      con.disconnect(ftpClient);
      ftpClient = null;
    }
    return count;
  }

  private String getUnqualifiedFilename(String s) {
    String result = s;
    int slashPos = -1;
    if (retrieveConnection(FileTransferConnection.class).windowsWorkaround()) {
      slashPos = s.lastIndexOf(BACK_SLASH);
    }
    else {
      slashPos = s.lastIndexOf(FORWARD_SLASH);
    }
    if (slashPos >= 0) {
      result = s.substring(slashPos + 1);
    }
    return result;
  }

  private boolean processMessage(String fullPath, String procDir) throws Exception {
    long olderThanMs = olderThanMs();
    if (additionalDebug) {
      log.trace("lastModified for [{}] is [{}]", fullPath, new Date(ftpClient.lastModified(fullPath)));
    }
    if (olderThanMs > 0) {
      long now = System.currentTimeMillis();
      long lastModified = ftpClient.lastModified(fullPath);
      if (!(now - lastModified >= olderThanMs)) {
        log.trace("[" + fullPath + "] not deemed safe to process, " + "lastModified on server=[" + new Date(lastModified)
            + "];file must be older than=[" + new Date(now - olderThanMs) + "]");
        return false;
      }
    }
    String wipFile = fullPath + wipSuffix();
    String filename = fullPath;
    int pos = fullPath.lastIndexOf(FORWARD_SLASH);
    if (pos >= 0 && fullPath.length() > pos) {
      filename = fullPath.substring(pos + 1);
    }
    if (additionalDebug) {
      log.trace("Renaming [" + fullPath + "] to [" + wipFile + "]");
    }
    ftpClient.rename(fullPath, wipFile);
    AdaptrisMessage adpMsg = null;
    if (getEncoder() == null) {
      adpMsg = defaultIfNull(getMessageFactory()).newMessage();
      OutputStream out = adpMsg.getOutputStream();
      ftpClient.get(out, wipFile);
      out.close();
    }
    else {
      byte[] remoteBytes = ftpClient.get(wipFile);
      adpMsg = decode(remoteBytes);
    }
    adpMsg.addMetadata(CoreConstants.ORIGINAL_NAME_KEY, filename);
    adpMsg.addMetadata(CoreConstants.FS_FILE_SIZE, "" + adpMsg.getSize());
    retrieveAdaptrisMessageListener().onAdaptrisMessage(adpMsg);

    if (procDir != null) {
      moveToProcDir(wipFile, filename, procDir);
    }
    else {
      ftpClient.delete(wipFile);
    }
    return true;
  }

  private void moveToProcDir(String wipFile, final String filename, String procDir) {

    String[] existingFileNames = null;

    try {
      existingFileNames = ftpClient.dir(procDir, new FileFilter() {

        @Override
        public boolean accept(File f) {
          boolean result = false;
          if (f.getName().equals(filename)) {
            result = true;
          }
          return result;
        }
      });

      if (existingFileNames.length == 0) {
        log.trace("Renaming processed file to " + procDir + FORWARD_SLASH + filename);
        ftpClient.rename(wipFile, procDir + FORWARD_SLASH + filename);
      }
      else {
        log.trace("Renaming processed file to " + procDir + FORWARD_SLASH + filename + "-" + System.currentTimeMillis());
        ftpClient.rename(wipFile, procDir + FORWARD_SLASH + filename + "-" + System.currentTimeMillis());
      }
    }
    catch (Exception e) {
      log.warn("Failed to rename to [" + filename + "] to " + procDir);
    }
  }

  @Override
  protected void prepareConsumer() throws CoreException {
  }


  /**
   * Get the "proc" directory.
   * 
   * @return the configured directory.
   */
  public String getProcDirectory() {
    return procDirectory;
  }

  /**
   * Get the work directory.
   * 
   * @return the work directory.
   */
  public String getWorkDirectory() {
    return workDirectory;
  }

  /**
   * Set the directory where files are placed after processing.
   * <p>
   * If set, then after downloading the file, it is renamed to this directory, otherwise it is deleted.
   * </p>
   * <p>
   * If the ConsumeDestination specifies a URL, then it is assumed be a sub-directory of the path specified by the URL. If the
   * ConsumeDestination does not specify a URL, then it is an absolute path.
   * </p>
   * 
   * @param s the directory
   */
  public void setProcDirectory(String s) {
    procDirectory = s;
  }

  /**
   * Set the work directory.
   * <p>
   * If the ConsumeDestination specifies a URL, then it is assumed be a sub-directory of the path specified by the URL. If the
   * ConsumeDestination does not specify a URL, then it is an absolute path.
   * </p>
   * 
   * @see com.adaptris.core.ConsumeDestination
   * @param s the directory.
   */
  public void setWorkDirectory(String s) {
    workDirectory = s;
  }

  String fileFilterImp() {
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
   * jakarta oro package to perform unix glob style filtering
   * @see com.adaptris.core.ConsumeDestination#getFilterExpression()
   */
  public void setFileFilterImp(String s) {
    fileFilterImp = s;
  }

  /**
   * @return Returns the wipSuffix.
   */
  public String getWipSuffix() {
    return wipSuffix;
  }

  /**
   * Return the wip Suffix with null protection.
   * 
   * @return the suffix, default is "_wip" if not configured.
   */
  String wipSuffix() {
    return getWipSuffix() != null ? getWipSuffix() : DEFAULT_WIP_SUFFIX;
  }

  /**
   * Set the suffix of the file to indicate it is being processed.
   * 
   * <p>
   * The first action performed by the consumer is to attempt to rename any file that it is attempting to process to mark it as
   * being processed. This will allow multiple consumers to poll the same directory, and also isolate the consumer from anything
   * that attempts to write to the file concurrently.
   * </p>
   * 
   * @param s The wipSuffix to set, default is "_wip" if not specified.
   */
  public void setWipSuffix(String s) {
    wipSuffix = s;
  }

  long olderThanMs() {
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
