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

import java.io.InputStream;
import java.io.OutputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.filetransfer.FileTransferClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
 * @see CoreConstants#FTP_REPLYTO_NAME
 * @see FileNameCreator
 * @see FtpConnection
 * @see SftpConnection
 * @see FileTransferConnection
 * @see ProduceDestination
 * @author lchan
 */
@XStreamAlias("ftp-producer")
@AdapterComponent
@ComponentProfile(summary = "Put a file on a FTP/SFTP server; uses PUT, RNFR and RNTO for atomicity",
    tag = "producer,ftp,ftps,sftp",
    recommended = {FtpConnection.class, FtpSslConnection.class, SftpConnection.class, SftpKeyAuthConnection.class})
public class FtpProducer extends RequestReplyProducerImp {

  private static final String SLASH = "/";

  private String destDirectory;
  private String buildDirectory;
  @AdvancedConfig
  private String replyDirectory = null;
  @AdvancedConfig
  private String replyProcDirectory = null;
  @AdvancedConfig
  private Boolean replyUsesEncoder;
  @NotNull
  @Valid
  @AutoPopulated
  private FileNameCreator filenameCreator;

  /**
   * Default Constructor with the following defaults.
   * <ul>
   * <li>fileNameCreator is <code>FormattedFilenameCreator</code></li>
   * <li>buildDirectory is /build</li>
   * <li>destDirectory is /work</li>
   * </ul>
   */
  public FtpProducer() {
    setFilenameCreator(new FormattedFilenameCreator());
    setDestDirectory("/work");
    setBuildDirectory("/build");
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    if (destDirectory == null) {
      throw new CoreException("No Destination directory specified");
    }
    if (buildDirectory == null) {
      throw new CoreException("No Build directory specified");
    }
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

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
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

  private FileNameCreator filenameCreatorToUse() {
    return getFilenameCreator();
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessageProducerImp#produce(AdaptrisMessage, ProduceDestination)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.produce()", logger = "com.adaptris.perf4j.ftp.TimingLogger")
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    FileTransferConnection conn = retrieveConnection(FileTransferConnection.class);
    FileTransferClient client = null;
    FileNameCreator creator = filenameCreatorToUse();

    try {
      client = conn.connect(destination.getDestination(msg));
      String dirRoot = conn.getDirectoryRoot(destination.getDestination(msg));
      String fileName = creator.createName(msg);
      String destFilename = dirRoot + destDirectory + SLASH + fileName;
      String buildFilename = dirRoot + buildDirectory + SLASH + fileName;
      if (conn.additionalDebug()) {
        log.trace("buildFilename=[" + buildFilename + "] destFilename=[" + destFilename + "]");
      }
      else {
        log.debug("destFilename=[" + destFilename + "]");
      }
      msg.addMetadata(CoreConstants.PRODUCED_NAME_KEY, fileName);
      if (getEncoder() != null) {
        byte[] bytesToWrite = encode(msg);
        client.put(bytesToWrite, buildFilename);
      }
      else {
        InputStream in = msg.getInputStream();
        client.put(in, buildFilename);
        in.close();
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

  /**
   *
   * @see RequestReplyProducerImp#doRequest(AdaptrisMessage, ProduceDestination, long)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.request()", logger = "com.adaptris.perf4j.ftp.TimingLogger")
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {

    if (replyDirectory == null) {
      throw new ProduceException("No Reply directory specified");
    }
    this.produce(msg, destination);
    try {
      log.trace("Waiting for [" + timeout + "] ms...");
      Thread.sleep(timeout);
    }
    catch (InterruptedException e) {
      ;
    }
    return handleReply(msg, destination);
  }

  private AdaptrisMessage handleReply(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    OutputStream out = null;
    FileTransferConnection conn = retrieveConnection(FileTransferConnection.class);
    FileTransferClient ftp = null;
    try {
      ftp = conn.connect(destination.getDestination(msg));
      String dirRoot = conn.getDirectoryRoot(destination.getDestination(msg));
      // String replyDir = dirRoot + SLASH + replyDirectory;
      // Remember that replyDirectory will have automatically had a "/" added to it.
      String replyDir = dirRoot + replyDirectory;
      String replyToName = null;
      if (msg.containsKey(CoreConstants.FTP_REPLYTO_NAME)) {
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
        out = reply.getOutputStream();
        ftp.get(out, replyFilePath);
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
      throw new ProduceException(e);
    }
    finally {
      IOUtils.closeQuietly(out);
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

  /**
   * Get the Reply Directory.
   *
   * @return string the Reply Directory
   */
  public String getReplyDirectory() {
    return replyDirectory;
  }

  /**
   * Set the Reply Directory.
   *
   * @param string the reply directory.
   */
  public void setReplyDirectory(String string) {
    replyDirectory = string;
  }

  /**
   * Get the ReplyProc Directory.
   *
   * @return string the ReplyProc Directory
   */
  public String getReplyProcDirectory() {
    return replyProcDirectory;
  }

  /**
   * Set the ReplyProc Directory.
   *
   * @param string the reply proc directory.
   */
  public void setReplyProcDirectory(String string) {
    replyProcDirectory = string;
  }

  public boolean replyUsesEncoder() {
    return getReplyUsesEncoder() != null ? getReplyUsesEncoder().booleanValue() : true;
  }
  /**
   * @return Returns the replyUsesEncoder.
   */
  public Boolean getReplyUsesEncoder() {
    return replyUsesEncoder;
  }

  /**
   * Set whether to use any configured encoder to parse the reply.
   *
   * @param b true or false; default true.
   */
  public void setReplyUsesEncoder(Boolean b) {
    replyUsesEncoder = b;
  }

  public FileNameCreator getFilenameCreator() {
    return filenameCreator;
  }

  /**
   * <p>
   * Sets the <code>FileNameCreator</code> to use. May not be null.
   * </p>
   *
   * @param creator the <code>FileNameCreator</code> to use
   */
  public void setFilenameCreator(FileNameCreator creator) {
    if (creator == null) {
      throw new IllegalArgumentException("FileNameCreator may not be null");
    }
    filenameCreator = creator;
  }
}
