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
import static com.adaptris.core.ftp.FtpHelper.FORWARD_SLASH;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.File;
import java.io.FileFilter;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
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
 * hierarchy with a key difference; although multiple file-filters can be configured only filters that work with the filepath will
 * work. Other filter implementations (such as those based on size /last modified) may not work.
 * </p>
 *
 * @config ftp-consumer
 *
 * @see FtpConnection
 * @see FileTransferConnection
 * @see com.adaptris.core.ConsumeDestination
 * @author lchan
 */
@XStreamAlias("ftp-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from an FTP or SFTP server", tag = "consumer,ftp,ftps,sftp", metadata =
{
        CoreConstants.ORIGINAL_NAME_KEY, CoreConstants.FS_FILE_SIZE,
        CoreConstants.FS_CONSUME_DIRECTORY, CoreConstants.MESSAGE_CONSUME_LOCATION
},
    recommended = {FileTransferConnection.class})
@DisplayOrder(order = {"ftpEndpoint", "filterExpression", "fileFilterImp", "poller",
    "workDirectory", "procDirectory", "wipSuffix", "quietInterval"})
public class FtpConsumer extends FtpConsumerImpl {
  private static final String DEFAULT_WIP_SUFFIX = "_wip";


  @NotNull
  @AutoPopulated
  private String workDirectory = "/work";
  @AdvancedConfig(rare = true)
  private String procDirectory;
  @AdvancedConfig(rare = true)
  private String wipSuffix;

  public FtpConsumer() {
    setReacquireLockBetweenMessages(true);
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    try {
      Args.notNull(getWorkDirectory(), "workDirectory");
      if (!workDirectory.startsWith(FORWARD_SLASH)) {
        workDirectory = FORWARD_SLASH + workDirectory;
      }
      if (procDirectory != null && !procDirectory.startsWith(FORWARD_SLASH)) {
        procDirectory = FORWARD_SLASH + procDirectory;
      }
      super.init();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected String configureWorkDir(String path) {
    if (!isEmpty(getWorkDirectory())) {
      return path + getWorkDirectory();
    }
    return super.configureWorkDir(path);
  }

  @Override
  protected boolean accept(String path) throws Exception {
    if (path.endsWith(wipSuffix())) {
      log.warn("[{}] matches [{}], assuming part processed and ignoring", path, wipSuffix());
      return false;
    }
    return super.accept(path);
  }

  @Override
  protected boolean fetchAndProcess(String fullPath) throws Exception {
    String procDir = null;
    String hostUrl = ftpURL();
    if (procDirectory != null) {
      procDir = retrieveConnection(FileTransferConnection.class).getDirectoryRoot(hostUrl)
          + procDirectory;
    }
    return processMessage(fullPath, procDir);
  }

  private boolean processMessage(String fullPath, String procDir) throws Exception {
    String wipFile = fullPath + wipSuffix();
    String filename = FtpHelper.getFilename(fullPath);
    if (additionalDebug()) {
      log.trace("Renaming [{}] to [{}]", fullPath, wipFile);
    }
    ftpClient.rename(fullPath, wipFile);
    EncoderWrapper encWrapper = new EncoderWrapper(defaultIfNull(getMessageFactory()).newMessage(), getEncoder());
    try (EncoderWrapper wrapper = encWrapper) {
      ftpClient.get(wrapper, wipFile);
    }
    AdaptrisMessage adpMsg =
        addStandardMetadata(encWrapper.build(), filename, FtpHelper.getDirectory(fullPath));
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

      String procFile = procDir + FORWARD_SLASH + filename;
      if (existingFileNames.length != 0) {
        procFile = procFile + "-" + System.currentTimeMillis();
      }
      log.trace("Renaming processed file to [{}]", procFile);
      ftpClient.rename(wipFile, procFile);
    }
    catch (Exception e) {
      log.warn("Failed to rename to [{}] to [{}]", filename, procDir);
    }
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


}
