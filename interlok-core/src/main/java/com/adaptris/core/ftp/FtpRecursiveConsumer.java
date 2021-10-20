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
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;
import java.io.IOException;

import static com.adaptris.core.ftp.FtpHelper.FORWARD_SLASH;

/**
 * FTP implementation of the AdaptrisMessageConsumer interface.
 * <p>
 * The connection type for this consumer should always be a concrete
 * implementation of {@link FileTransferConnection}.
 * </p>
 * <p>
 * The destination should be in the form in the URL form dictated by the
 * <code>FileTransferConnection</code> flavour or simply the IP
 * Address/DNS name of the target Server. If the URL form is used, then
 * it is possible to override the username, password, and port settings
 * of the server, in all other cases the configuration specified in the
 * <code>FileTransferConnection</code> object will be used.
 * </p>
 * <p>
 * In the event the proc-directory is not configured, then after
 * processing the file, it is deleted. If proc-directory is configured,
 * then the remote file will be renamed to this directory
 * </p>
 * <p>
 * The configuration of this consumer closely mirrors that of the
 * FsConsumer though it does not, at the moment, share any common
 * hierarchy with a key difference; although multiple file-filters can
 * be configured only filters that work with the filepath will work.
 * Other filter implementations (such as those based on size /last
 * modified) may not work.
 * </p>
 * <p>
 * Unlike the original FTP consumer, this will recurse into any
 * directories found.
 * </p>
 *
 * @author lchan
 * @config ftp-recursive-consumer
 * @see FtpConnection
 * @see FtpConsumer
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

  @Override
  protected AdaptrisMessage addStandardMetadata(AdaptrisMessage msg, String filename, String dir) {
    super.addStandardMetadata(msg, filename, dir);
    File parent = new File(dir);
    if (parent != null) {
      msg.addMetadata(CoreConstants.FS_CONSUME_PARENT_DIR, parent.getName());
    }
    return msg;
  }

}
