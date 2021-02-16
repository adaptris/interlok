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
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.InputStream;
import java.io.OutputStream;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

/**
 * Ftp implementation of the AdaptrisMessageProducer interface.
 *
 * <p>
 * The connection type for this implementation should always be a concrete subclass of <code>FileTransferConnection</code> such as
 * <code>FtpConnection</code> or <code>SftpConnection</code>
 * </p>
 * <p>
 * The destination returned by the ProduceDestination implementation should be in the URL form dictated by the
 * <code>FileTransferConnection</code> flavour or simply the IP Address / DNS name of the FTP/SFTP Server. If the URL form is used,
 * then it is possible to override the username, password, and port settings of the FTP server, in all other cases the configuration
 * specified in the <code>FileTransferConnection</code> object will be used.
 * </p>
 * <p>
 * The output filename is controlled using the standard FileNameCreator interface. The AdaptrisMessage object is first uploaded to
 * the build-directory and subsequently renamed into the dest-directory. In most FTP Server implementations, the rename can be
 * considered an atomic operation, so using this method reduces the liklehood of file read attempts during the upload.
 * </p>
 * <p>
 * It is possible to perform limited request retry functionality using this implementation, in this instance
 * <code>reply-directory</code> element must be set. <b>We strongly discourage anyone else from trying to implement request reply
 * using FTP</b>, it is dangerous and prone to undefined behaviour.
 * </p>
 * <p>
 * When running in request reply mode, the following steps occur after producing the file in the normal fashion.
 * <ol>
 * <li>Pause operations for the request timeout length for which the default is 60000ms</li>
 * <li>Attempt to read a file that has the same name as that uploaded from <code>reply-directory</code></li>
 * <li>If <code>reply-proc-directory</code> is set, then move the file to this directory, otherwise delete it</li>
 * <li>Use the contents of this file as the content of the reply</li>
 * </ol>
 * </p>
 * <p>
 * In the situation where a specific file should be treated as the reply, then the metadata key corresponding to
 * <code>CoreConstants#FTP_REPLYTO_NAME</code> should be populated.
 * </p>
 *
 * @config ftp-producer
 *
 * @see FileNameCreator
 * @see FtpConnection
 * @see FileTransferConnection
 */
@XStreamAlias("ftp-producer")
@AdapterComponent
@ComponentProfile(summary = "Put a file on a FTP/SFTP server; uses PUT, RNFR and RNTO for atomicity",
tag = "producer,ftp,ftps,sftp",
recommended = {FileTransferConnection.class})
@DisplayOrder(
    order = {"ftpEndpoint", "buildDirectory", "destDirectory", "replyDirectory",
    "replyProcDirectory"})
public class FtpProducer extends RequestReplyProducerImp {

  private static final String SLASH = "/";

  private String destDirectory;
  private String buildDirectory;
  /**
   * Once a file is deposited wait for a reply to appear in the specified directory.
   * <p>
   * This is a legacy feature that was enabled for certain customers that thought it would be a
   * really good idea to try and do request reply via FTP.
   * </p>
   *
   * @see CoreConstants#FTP_REPLYTO_NAME
   * @see CoreConstants#PRODUCED_NAME_KEY
   */
  @AdvancedConfig(rare = true)
  @Getter
  @Setter
  @Deprecated
  @ConfigDeprecated(removalVersion = "4.0.0", message = "We strongly discourage anyone from trying to implement request reply using FTP", groups = Deprecated.class)
  private String replyDirectory = null;
  /**
   * Once the reply has been handled move it here.
   * <p>
   * This is a legacy feature that was enabled for certain customers that thought it would be a
   * really good idea to try and do request reply via FTP.
   * </p>
   *
   */
  @AdvancedConfig(rare = true)
  @Getter
  @Setter
  @Deprecated
  @ConfigDeprecated(removalVersion = "4.0.0", message = "We strongly discourage anyone from trying to implement request reply using FTP", groups = Deprecated.class)
  private String replyProcDirectory = null;

  /**
   * Whether or not the reply will be encoded.
   * <p>
   * This is a legacy feature that was enabled for certain customers that thought it would be a
   * really good idea to try and do request reply via FTP.
   * </p>
   * <p>
   * The default is true, because otherwise how else can you transfer metadata?
   * </p>
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  @Deprecated
  @ConfigDeprecated(removalVersion = "4.0.0", message = "We strongly discourage anyone from trying to implement request reply using FTP", groups = Deprecated.class)
  private Boolean replyUsesEncoder;

  @Valid
  private FileNameCreator filenameCreator;

  /**
   * The FTP endpoint in which to deposit files.
   * <p>
   * Although nominally a URL, you can configure the following styles
   * <ul>
   * <li>Just the server name / IP Address (e.g. 10.0.0.1) in which case the username and password
   * from the corresponding {@link FileTransferConnection} will be used to supply the username and
   * password. The destination directory will be {@code /work}</li>
   * <li>A FTP style URL {@code ftp://10.0.0.1/path/to/dir}, the username and password will be taken
   * from the corresponding connection. The destination directory will be
   * {@code /path/to/dir/work}</li>
   * <li>A FTP style URL with a username/password {@code ftp://user:password@10.0.0.1/path/to/dir}.
   * The destination directory will be {@code /path/to/dir/work}</li>
   * <li>An expression that resolves to one of the above values '%message{myFtpServer}'</li>
   * </ul>
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  @NotBlank
  private String ftpEndpoint;
  private transient boolean destWarning;
  private transient boolean requestReplyWarning;


  /**
   * Default Constructor with the following defaults.
   * <ul>
   * <li>buildDirectory is /build</li>
   * <li>destDirectory is /work</li>
   * </ul>
   */
  public FtpProducer() {
    setDestDirectory("/work");
    setBuildDirectory("/build");
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    try {
      Args.notNull(getDestDirectory(), "destDirectory");
      Args.notNull(getBuildDirectory(), "buildDirectory");
      if (!destDirectory.startsWith(SLASH)) {
        destDirectory = SLASH + destDirectory;
      }
      if (!buildDirectory.startsWith(SLASH)) {
        buildDirectory = SLASH + buildDirectory;
      }
      if (replyDirectory != null && !replyDirectory.startsWith(SLASH)) {
        replyDirectory = SLASH + replyDirectory;
      }
      if (replyProcDirectory != null && !replyProcDirectory.startsWith(SLASH)) {
        replyProcDirectory = SLASH + replyProcDirectory;
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }

  }

  /**
   * The default is 1 minute (60000 ms).
   *
   * @see com.adaptris.core.RequestReplyProducerImp#defaultTimeout()
   */
  @Override
  protected long defaultTimeout() {
    return 60000;
  }

  FileNameCreator filenameCreatorToUse() {
    return ObjectUtils.defaultIfNull(getFilenameCreator(), new FormattedFilenameCreator());
  }


  @Override
  public void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    FileTransferConnection conn = retrieveConnection(FileTransferConnection.class);
    FileTransferClient client = null;
    FileNameCreator creator = filenameCreatorToUse();

    try {
      client = conn.connect(endpoint);
      String dirRoot = conn.getDirectoryRoot(endpoint);
      String fileName = creator.createName(msg);
      String destFilename = dirRoot + destDirectory + SLASH + fileName;
      String buildFilename = dirRoot + buildDirectory + SLASH + fileName;
      if (conn.additionalDebug()) {
        log.trace("buildFilename=[{}], destFilename=[{}]", buildFilename, destFilename);
      }
      else {
        log.debug("destFilename=[{}]", destFilename);
      }
      msg.addMetadata(CoreConstants.PRODUCED_NAME_KEY, fileName);
      if (getEncoder() != null) {
        byte[] bytesToWrite = encode(msg);
        client.put(bytesToWrite, buildFilename);
      }
      else {
        try (InputStream in = msg.getInputStream()) {
          client.put(in, buildFilename);
        }
      }
      client.rename(buildFilename, destFilename);
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
    finally {
      conn.disconnect(client);
    }
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, String endpoint, long timeout)
      throws ProduceException {

    if (replyDirectory == null) {
      throw new ProduceException("No Reply directory specified");
    }
    doProduce(msg, endpoint);
    LifecycleHelper.waitQuietly(timeout);
    return handleReply(msg, endpoint);
  }

  private AdaptrisMessage handleReply(AdaptrisMessage msg, String endpoint)
      throws ProduceException {
    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    FileTransferConnection conn = retrieveConnection(FileTransferConnection.class);
    FileTransferClient ftp = null;
    try {
      ftp = conn.connect(endpoint);
      String dirRoot = conn.getDirectoryRoot(endpoint);
      // String replyDir = dirRoot + SLASH + replyDirectory;
      // Remember that replyDirectory will have automatically had a "/" added to it.
      String replyDir = dirRoot + replyDirectory;
      String replyToName = null;
      if (msg.headersContainsKey(CoreConstants.FTP_REPLYTO_NAME)) {
        replyToName = msg.getMetadataValue(CoreConstants.FTP_REPLYTO_NAME);
      }
      else {
        replyToName = msg.getMetadataValue(CoreConstants.PRODUCED_NAME_KEY);
      }
      String replyFilePath = replyDir + SLASH + replyToName;
      if (conn.additionalDebug()) {
        log.trace("Expecting to retrieve [" + replyFilePath + "] from remote server");
      }
      if (replyUsesEncoder()) {
        reply = decode(ftp.get(replyFilePath));
      }
      else {
        try (OutputStream out = reply.getOutputStream()) {
          ftp.get(out, replyFilePath);
        }
      }
      if (replyProcDirectory != null) {
        // Remember that replyProcDirectory will have automatically had a "/" added to it.
        String replyProcDir = dirRoot + replyProcDirectory;
        ftp.rename(replyFilePath, replyProcDir + SLASH + replyToName);
      }
      else {
        ftp.delete(replyFilePath);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    finally {
      conn.disconnect(ftp);
    }
    return reply;
  }

  @Override
  public void prepare() throws CoreException {

  }

  /**
   * Get the build directory.
   *
   * @return the build directory.
   */
  public String getBuildDirectory() {
    return buildDirectory;
  }

  /**
   * Get the destination directory.
   *
   * @return the destination directory.
   */
  public String getDestDirectory() {
    return destDirectory;
  }

  /**
   * Set the build directory.
   *
   * @param string the dir.
   */
  public void setBuildDirectory(String string) {
    buildDirectory = string;
  }

  /**
   * Set the dest directory.
   *
   * @param string the dir.
   */
  public void setDestDirectory(String string) {
    destDirectory = string;
  }

  @Deprecated
  @Removal(version = "4.0.0")
  public boolean replyUsesEncoder() {
    return BooleanUtils.toBooleanDefaultIfNull(getReplyUsesEncoder(), true);
  }

  public FileNameCreator getFilenameCreator() {
    return filenameCreator;
  }

  public void setFilenameCreator(FileNameCreator creator) {
    filenameCreator = creator;
  }


  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return msg.resolve(getFtpEndpoint());
  }
}
