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
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.filetransfer.FileTransferClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Ftp implementation of the AdaptrisMessageProducer interface.
 *
 * <p>
 * The connection type for this implementation should always be a concrete subclass of
 * <code>FileTransferConnection</code> such as <code>FtpConnection</code> or
 * <code>SftpConnection</code>
 * </p>
 * <p>
 * The destination returned by the {@link #getFtpEndpoint()} should be in the URL form dictated by
 * the <code>FileTransferConnection</code> flavour or simply the IP Address / DNS name of the
 * FTP/SFTP Server. If the URL form is used, then it is possible to override the username, password,
 * and port settings of the FTP server, in all other cases the configuration specified in the
 * <code>FileTransferConnection</code> object will be used.
 * </p>
 * <p>
 * The output filename is controlled using the standard FileNameCreator interface. The
 * AdaptrisMessage object is first uploaded to the build-directory and subsequently renamed into the
 * dest-directory. In most FTP Server implementations, the rename can be considered an atomic
 * operation, so using this method reduces the liklehood of file read attempts during the upload.
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
public class FtpProducer extends ProduceOnlyProducerImp {

  private static final String SLASH = "/";

  private String destDirectory;
  private String buildDirectory;

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
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }

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
