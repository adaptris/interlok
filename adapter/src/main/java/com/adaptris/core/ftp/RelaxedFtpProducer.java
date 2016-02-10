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

import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.filetransfer.FileTransferClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Ftp producer implementation.
 * <p>
 * This differs from the standard {@link FtpProducer} as it does not use a staging area to upload file, nor does it rename the file
 * from the staging directory into the destination directory once the upload is complete. It simply writes directly to the specified
 * directory.
 * </p>
 * <p>
 * If the remote system starts processing the file before upload is complete then you may encounter undefined behaviour in your
 * back-end system. If the remote file is deleted before upload is complete, then depending on the server, this may not generate any
 * errors, and you will have a partial file that the adapter thinks was successfully delivered. <strong>There are lots of ways in
 * which this can go wrong</strong>. It is not recommended that you use this FtpProducer unless there are very specific reasons to;
 * e.g. the remote FTP server does not support the RNFR and RNTO command.
 * </p>
 * <p>
 * The destination returned by the ProduceDestination implementation should be in the form in the URL form dictated by the
 * {@link FileTransferConnection} flavour including the directory from which you wish to write to. Simply specifying the IP Address
 * or DNS name of the remote ftp server will cause files to be written without specifying a directory (which if you are not in an
 * ftp chroot jail might be a very bad thing).
 * </p>
 * <p>
 * This implementation does not support RequestReply.
 * </p>
 * 
 * @config relaxed-ftp-producer
 * 
 * @see FileNameCreator
 * @see FtpConnection
 * @see SftpConnection
 * @see FileTransferConnection
 * @see ProduceDestination
 * @author lchan
 */
@XStreamAlias("relaxed-ftp-producer")
@AdapterComponent
@ComponentProfile(summary = "Put a file on a FTP/SFTP server; uses PUT only", tag = "producer,ftp,ftps,sftp",
    recommended = {FileTransferConnection.class})
public class RelaxedFtpProducer extends ProduceOnlyProducerImp {

  private static final String SLASH = "/";

  @NotNull
  @Valid
  @AutoPopulated
  private FileNameCreator fileNameCreator;

  /**
   * Default Constructor with the following defaults.
   * <ul>
   * <li>fileNameCreator is {@link FormattedFilenameCreator}</li>
   * </ul>
   */
  public RelaxedFtpProducer() {
    setFileNameCreator(new FormattedFilenameCreator());
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
   *
   * @see com.adaptris.core.AdaptrisMessageProducerImp#produce(AdaptrisMessage, ProduceDestination)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.produce()", logger = "com.adaptris.perf4j.ftp.TimingLogger")
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    FileTransferConnection conn = retrieveConnection(FileTransferConnection.class);
    FileTransferClient client = null;
    try {
      client = conn.connect(destination.getDestination(msg));
      String dirRoot = conn.getDirectoryRoot(destination.getDestination(msg));
      String fileName = fileNameCreator.createName(msg);
      String destFilename = dirRoot + SLASH + fileName;
      if (dirRoot.endsWith(SLASH)) {
        destFilename = dirRoot + fileName;
      }
      log.debug("destFilename=[" + destFilename + "]");
      msg.addMetadata(CoreConstants.PRODUCED_NAME_KEY, fileName);
      if (getEncoder() != null) {
        byte[] bytesToWrite = encode(msg);
        client.put(bytesToWrite, destFilename);
      }
      else {
        InputStream in = msg.getInputStream();
        client.put(in, destFilename);
        in.close();
      }
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
    finally {
      conn.disconnect(client);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }


  /**
   * <p>
   * Returns the <code>FileNameCreator</code> used by this object.
   * </p>
   *
   * @return the <code>FileNameCreator</code> used by this object
   */
  public FileNameCreator getFileNameCreator() {
    return fileNameCreator;
  }

  /**
   * <p>
   * Sets the <code>FileNameCreator</code> to use. May not be null.
   * </p>
   *
   * @param creator the <code>FileNameCreator</code> to use
   */
  public void setFileNameCreator(FileNameCreator creator) {
    if (creator == null) {
      throw new IllegalArgumentException("FileNameCreator may not be null");
    }
    fileNameCreator = creator;
  }
}
