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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.IOException;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.ftp.FtpHelper.FORWARD_SLASH;

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
 * <p>
 * TODO Update about recursion...
 *
 * @author lchan
 * @config ftp-consumer
 * @see FtpConnection
 * @see FileTransferConnection
 */
@XStreamAlias("ftp-recursive-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from an FTP or SFTP server", tag = "consumer,ftp,ftps,sftp,recursive", metadata =
    {
        CoreConstants.ORIGINAL_NAME_KEY, CoreConstants.FS_FILE_SIZE,
        CoreConstants.FS_CONSUME_DIRECTORY, CoreConstants.MESSAGE_CONSUME_LOCATION
    },
    recommended = { FileTransferConnection.class })
@DisplayOrder(order = { "ftpEndpoint", "filterExpression", "fileFilterImp", "poller",
    "workDirectory", "procDirectory", "wipSuffix", "quietInterval" })
public class FtpRecursiveConsumer extends FtpConsumer
{

  private static final String DEFAULT_WIP_SUFFIX = "_wip";

  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  private String workDirectory = "/work";

  @Getter
  @Setter
  @AdvancedConfig(rare = true)
  private String procDirectory;

  @Getter
  @Setter
  @AdvancedConfig(rare = true)
  private String wipSuffix;

  public FtpRecursiveConsumer()
  {
    setReacquireLockBetweenMessages(true);
  }

  @Override
  protected boolean fetchAndProcess(String fullPath) throws Exception
  {
    String procDir = null;
    String hostUrl = ftpURL();
    if (procDirectory != null)
    {
      procDir = retrieveConnection(FileTransferConnection.class).getDirectoryRoot(hostUrl) + procDirectory;
    }
    return processMessage(fullPath, procDir);
  }

  @Override
  protected int processMessages()
  {
    String pollDirectory;
    FileTransferConnection connection = retrieveConnection(FileTransferConnection.class);
    String hostUrl = ftpURL();
    try
    {
      ftpClient = connection.connect(hostUrl);
      pollDirectory = configureWorkDir(connection.getDirectoryRoot(hostUrl));
    }
    catch (Exception e)
    {
      log.error("Failed to connect to [{}]", hostUrl, e);
      return 0;
    }
    try
    {
      return processMessages(connection, pollDirectory);
    }
    catch (Exception e)
    {
      log.warn("Failed to poll [{}] hoping for success next poll time", pollDirectory);
      if (additionalDebug())
      {
        log.trace("Exception was : {}", e.getMessage(), e);
      }
      return 0;
    }
    finally
    {
      connection.disconnect(ftpClient);
      ftpClient = null;
    }
  }

  private int processMessages(FileTransferConnection connection, String path) throws IOException
  {
    int count = 0;
    if (additionalDebug())
    {
      log.trace("Polling [{}]", path);
    }
    String[] files = ftpClient.dir(path, fileFilter);
    if (additionalDebug())
    {
      log.trace("There are potentially [{}] more messages to process", files.length);
    }
    for (String file : files)
    {
      String fileToGet = path + FORWARD_SLASH + FtpHelper.getFilename(file, connection.windowsWorkaround());

      boolean isDirectory = ftpClient.isDirectory(fileToGet);
      if (isDirectory)
      {
        count += processMessages(connection, fileToGet);
      }
      else
      {
        count += handle(fileToGet) ? 1 : 0;
      }
      if (!continueProcessingMessages(count))
      {
        break;
      }
    }
    return count;
  }

  private boolean processMessage(String fullPath, String procDir) throws Exception
  {
    String wipFile = fullPath + wipSuffix();
    String filename = FtpHelper.getFilename(fullPath);
    if (additionalDebug())
    {
      log.trace("Renaming [{}] to [{}]", fullPath, wipFile);
    }
    ftpClient.rename(fullPath, wipFile);
    EncoderWrapper encWrapper = new EncoderWrapper(defaultIfNull(getMessageFactory()).newMessage(), getEncoder());
    try (EncoderWrapper wrapper = encWrapper)
    {
      ftpClient.get(wrapper, wipFile);
    }
    AdaptrisMessage adpMsg = addStandardMetadata(encWrapper.build(), filename, FtpHelper.getDirectory(fullPath));
    retrieveAdaptrisMessageListener().onAdaptrisMessage(adpMsg);

    if (procDir != null)
    {
      moveToProcDir(wipFile, filename, procDir);
    }
    else
    {
      ftpClient.delete(wipFile);
    }
    return true;
  }

  private void moveToProcDir(String wipFile, final String filename, String procDir)
  {
    try
    {
      String[] existingFileNames = ftpClient.dir(procDir, f ->
      {
        boolean result = f.getName().equals(filename);
        return result;
      });

      String procFile = procDir + FORWARD_SLASH + filename;
      if (existingFileNames.length != 0)
      {
        procFile = procFile + "-" + System.currentTimeMillis();
      }
      log.trace("Renaming processed file to [{}]", procFile);
      ftpClient.rename(wipFile, procFile);
    }
    catch (Exception e)
    {
      log.warn("Failed to rename to [{}] to [{}]", filename, procDir);
    }
  }

  /**
   * Return the wip Suffix with null protection.
   *
   * @return the suffix, default is "_wip" if not configured.
   */
  String wipSuffix()
  {
    return getWipSuffix() != null ? getWipSuffix() : DEFAULT_WIP_SUFFIX;
  }

}
