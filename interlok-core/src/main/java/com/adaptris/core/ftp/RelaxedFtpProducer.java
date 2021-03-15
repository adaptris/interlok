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
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.filetransfer.FileTransferClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.InputStream;

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
 * @see FileTransferConnection
 * @author lchan
 */
@XStreamAlias("relaxed-ftp-producer")
@AdapterComponent
@ComponentProfile(summary = "Put a file on a FTP/SFTP server; uses PUT only", tag = "producer,ftp,ftps,sftp",
recommended = {FileTransferConnection.class})
@DisplayOrder(order = {"ftpEndpoint", "filenameCreator"})
public class RelaxedFtpProducer extends ProduceOnlyProducerImp {

  private static final String SLASH = "/";

  @Valid
  @InputFieldDefault(value = "formatted-filename-creator")
  @Getter
  @Setter
  private FileNameCreator filenameCreator;

  /**
   * The FTP endpoint in which to deposit files.
   * <p>
   * Although nominal a URL, you can configure the following styles
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

  public RelaxedFtpProducer() {
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessageProducerImp#produce(AdaptrisMessage)
   */
  @Override
  public void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    FileTransferConnection conn = retrieveConnection(FileTransferConnection.class);
    FileTransferClient client = null;
    try {
      client = conn.connect(endpoint);
      String dirRoot = conn.getDirectoryRoot(endpoint);
      String fileName = filenameCreator().createName(msg);
      String destFilename = dirRoot + SLASH + fileName;
      if (dirRoot.endsWith(SLASH)) {
        destFilename = dirRoot + fileName;
      }
      log.debug("destFilename=[{}]", destFilename);
      msg.addMetadata(CoreConstants.PRODUCED_NAME_KEY, fileName);
      if (getEncoder() != null) {
        byte[] bytesToWrite = encode(msg);
        client.put(bytesToWrite, destFilename);
      }
      else {
        try (InputStream in = msg.getInputStream()) {
          client.put(in, destFilename);
        }
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

  FileNameCreator filenameCreator() {
    return ObjectUtils.defaultIfNull(getFilenameCreator(), new FormattedFilenameCreator());
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return msg.resolve(getFtpEndpoint());
  }
}
