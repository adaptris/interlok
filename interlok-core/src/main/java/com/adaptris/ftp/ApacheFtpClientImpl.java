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

package com.adaptris.ftp;

import java.io.ByteArrayOutputStream;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import com.adaptris.core.util.Args;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferClientImp;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.util.FifoMutexLock;

/**
 * Base implementation of {@link FileTransferClient} that uses the apache
 * commons net FTP implementation.
 * 
 */
public abstract class ApacheFtpClientImpl<T extends FTPClient> extends FileTransferClientImp implements FtpFileTransferClient {

  private transient T ftp;
  private transient TimezoneDateHandler tzHandler;

  private transient String remoteHost;
  private transient int timeout;
  private transient int port;
  protected transient Map<String, String> additionalSettings;


  // This is to handle possible thread safety from Usage from
  // FileTransferConnection
  private transient FifoMutexLock lock;


  private ApacheFtpClientImpl() {
    super();
    lock = new FifoMutexLock();
    additionalSettings = new HashMap<String, String>();
  }

  /**
   * Constructor
   * 
   * @param remoteHost the remote hostname
   * @param port connection port
   * @param timeout connection timeout
   */
  public ApacheFtpClientImpl(String remoteHost, int port, int timeout) throws IOException {
    this();
    this.remoteHost = remoteHost;
    this.port = port;
    setTimeout(timeout);
    tzHandler = new TimezoneDateHandler(TimeZone.getDefault());
  }

  public <S extends ApacheFtpClientImpl> S withAdditionalSettings(Map<String, String> settings) {
    additionalSettings = Args.notNull(settings, "additionalSettings");
    return (S) this;
  }

  private T ftpClient() throws IOException {
    if (ftp == null) {
      ftp = createFTPClient();
      preConnectSettings(ftp);
      ftp.setConnectTimeout(getTimeout());
      ftp.setDataTimeout(getTimeout());
      try {
        ftp.connect(remoteHost, port);
        logReply(ftp.getReplyStrings());
        int replyCode = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
          throw new IOException("FTP Server refused connection");
        }
        postConnectSettings(ftp);
      } catch (IOException | RuntimeException e) {
        if (ftp.isConnected()) {
          ftp.disconnect();
          ftp = null;
        }
        throw e;
      }
    }
    return ftp;
  }

  /**
   * Create the base commons net client.
   * 
   * @return the actual FtpClient implementation that will be used.
   */
  protected abstract T createFTPClient();

  protected abstract void preConnectSettings(T client) throws IOException;

  protected abstract void postConnectSettings(T client) throws IOException;

  public int getTimeout() throws IOException {
    return timeout;
  }

  public void setTimeout(int millis) {
    timeout = millis;
  }

  private void acquireLock() {
    try {
      lock.acquire();
    }
    catch (InterruptedException e) {
      throw new RuntimeException();
    }
  }

  private void releaseLock() {
    lock.release();
  }

  @Override
  public void connect(String user, String password) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.USER, user);
      log("{} ********", FTPCmd.PASS);
      handleReturnValue(ftpClient().login(user, password));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  @Override
  public void connect(String user, String password, String account) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.USER, user);
      log("{} ********", FTPCmd.PASS);
      log("{} {}", FTPCmd.ACCT, user);
      handleReturnValue(ftpClient().login(user, password, account));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  @Override
  public void put(InputStream srcStream, String remoteFile, boolean append) throws IOException {
    try {
      acquireLock();
      if (append) {
        log("{} {}", FTPCmd.APPE, remoteFile);
        handleReturnValue(ftpClient().appendFile(remoteFile, srcStream));
      }
      else {
        log("{} {}", FTPCmd.STOR, remoteFile);
        handleReturnValue(ftpClient().storeFile(remoteFile, srcStream));
      }
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  /**
   * Get data from a remote file
   * 
   * @param destStream output target data stream
   * @param remoteFile file to be read on the server
   */
  @Override
  public void get(OutputStream destStream, String remoteFile) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.RETR, remoteFile);
      handleReturnValue(ftpClient().retrieveFile(remoteFile, destStream));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  /**
   * Get data as a byte array from a server file
   * 
   * @param remoteFile file to be read on the server
   */
  @Override
  public byte[] get(String remoteFile) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
    try {
      acquireLock();
      log("{} {}", FTPCmd.RETR, remoteFile);
      try (InputStream i = ftpClient().retrieveFileStream(remoteFile); OutputStream o = out) {
        IOUtils.copy(i, o);
      }
      ftpClient().completePendingCommand();
      logReply(ftpClient().getReplyStrings());
      log("Transferred {} bytes from remote host", out.size());
    }
    finally {
      releaseLock();
    }
    return out.toByteArray();
  }


  @Override
  public String[] dir(String directory, FileFilter filter) throws FileTransferException, IOException {
    return filter(dir(directory), filter);
  }

  /**
   * list files in the current server directory
   */
  @Override
  public String[] dir(String dirname, boolean full) throws IOException {
    String[] listing = new String[0];
    try {
      acquireLock();
      log("{} {}", FTPCmd.LIST, dirname);
      FTPFile[] results = ftpClient().listFiles(dirname);
      logReply(ftpClient().getReplyStrings());
      List<String> output = new ArrayList<String>();
      for (FTPFile file : results) {
        if (full) {
          output.add(file.toFormattedString());
        }
        else {
          output.add(file.getName());
        }
        listing = output.toArray(new String[output.size()]);
      }
    }
    finally {
      releaseLock();
    }
    return listing;
  }

  /**
   * delete a file on the server
   * 
   * @param remoteFile to be deleted
   */
  @Override
  public void delete(String remoteFile) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.DELE, remoteFile);
      handleReturnValue(ftpClient().deleteFile(remoteFile));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  /**
   * rename a file on the server
   * 
   * @param from file to be renamed
   * @param to new name for file
   */
  @Override
  public void rename(String from, String to) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.RNFR, from);
      log("{} {}", FTPCmd.RNTO, to);
      handleReturnValue(ftpClient().rename(from, to));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  /**
   * remove a directory from the server
   * 
   * @param dir directory name
   */
  @Override
  public void rmdir(String dir) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.RMD, dir);
      handleReturnValue(ftpClient().removeDirectory(dir));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }

  }

  /**
   * create a directory on the server
   * 
   * @param dir directory name
   */
  @Override
  public void mkdir(String dir) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.MKD, dir);
      handleReturnValue(ftpClient().makeDirectory(dir));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  /**
   * change directory on the server
   * 
   * @param dir directory name
   */
  @Override
  public void chdir(String dir) throws IOException {
    try {
      acquireLock();
      log("{} {}", FTPCmd.CWD, dir);
      handleReturnValue(ftpClient().changeWorkingDirectory(dir));
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
  }

  /**
   * disconnect from the server
   */
  @Override
  public void disconnect() throws IOException {
    try {
      acquireLock();
      log("BYE");
      ftpClient().disconnect();
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
      ftp = null;
    }
  }

  /**
   * Set transfer type eg. ASCII, BINARY
   * 
   * @param ftpFileType FILE_TYPE constant from the Apache commons net FtpClient
   *          class
   */
  public void setType(TransferType ftpFileType) throws IOException {
    ftpFileType.applyTransferType(ftpClient());
  }

  /**
   * Get last modified date time of file
   */
  @Override
  public long lastModified(String path) throws IOException {
    long lastModified = 0;
    try {
      acquireLock();
      log("MDTM {}", path);
      ftpClient().sendCommand("MDTM", path);
      Reply reply = validateReply(ftpClient().getReplyString(), "213");
      lastModified = tzHandler.asLong(reply.getReplyText());
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
    return lastModified;
  }

  /**
   * Get last modified date time of file
   */
  @Override
  public Date lastModifiedDate(String path) throws IOException {
    Date lastModified = null;
    try {
      acquireLock();
      log("MDTM {}", path);
      ftpClient().sendCommand("MDTM", path);
      Reply reply = validateReply(ftpClient().getReplyString(), "213");
      lastModified = tzHandler.asDate(reply.getReplyText());
    }
    finally {
      logReply(ftpClient().getReplyStrings());
      releaseLock();
    }
    return lastModified;
  }

  /**
   * Set the FTP Server timezone handler for modification times.
   * 
   * If not explicitly set, then the server is assumed to be in the same
   * timezone as the client; this could lead to incorrect modification times
   * being reported.
   * 
   * @param tz the handler.
   */
  public void setServerTimezone(TimeZone tz) {
    tzHandler = new TimezoneDateHandler(tz);
  }

  private void logReply(String[] replyText) {
    if (replyText == null || replyText.length == 0) {
      return;
    }
    if (isAdditionaDebug()) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < replyText.length; i++) {
        sb.append(replyText[i]);
        if (i + 1 < replyText.length) {
          sb.append(System.lineSeparator());
        }
      }
      logR.trace(sb.toString());
    }
  }

  /**
   * Get the type of the OS at the server
   * 
   * @return the type of server OS
   * @throws IOException if a comms error occurs
   */
  public String system() throws IOException {
    String result = null;
    try {
      acquireLock();
      ftpClient().syst();
      result = ftpClient().getReplyString();
    }
    finally {
      releaseLock();
    }
    return result;
  }

  /**
   * Get the current working directory on the server
   * 
   * @return the directory
   * @throws IOException if a comms error occurs
   */
  public String pwd() throws IOException {
    String result = null;
    try {
      acquireLock();
      ftpClient().pwd();
      result = ftpClient().getReplyString();
    }
    finally {
      releaseLock();
    }
    return result;
  }

  private class TimezoneDateHandler {

    /**
     * Format to interpret MTDM timestamp
     */
    private transient SimpleDateFormat tsFormat;

    TimezoneDateHandler(TimeZone tz) {
      tsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      if (tz != null) {
        tsFormat.setTimeZone(tz);
      }
    }

    Date asDate(String mdtmString) {
      return tsFormat.parse(mdtmString, new ParsePosition(0));
    }

    long asLong(String mdtmString) {
      return asDate(mdtmString).getTime();
    }
  }

  public void setDataMode(FtpDataMode mode) throws IOException {
    try {
      acquireLock();
      mode.applyDataMode(ftpClient());
    }
    finally {
      releaseLock();
    }
  }

  /**
   * Validate the response the host has supplied against the expected reply. If
   * we get an unexpected reply we throw an exception, setting the message to
   * that returned by the FTP server
   * 
   * @param reply the entire reply string we received
   * @param expectedReplyCode the reply we expected to receive
   * 
   */
  private Reply validateReply(String reply, String expectedReplyCode) throws IOException, FileTransferException {

    // all reply codes are 3 chars long
    String replyCode = reply.substring(0, 3);
    String replyText = reply.substring(4);
    Reply replyObj = new Reply(replyCode, replyText);

    if (replyCode.equals(expectedReplyCode)) {
      return replyObj;
    }

    // if unexpected reply, throw an exception
    throw new FtpException(replyText, replyCode);
  }


  @Override
  public long getKeepAliveTimeout() throws FileTransferException {
    try {
      acquireLock();
      return ftpClient().getControlKeepAliveTimeout();
    } catch (IOException e) {
      throw new FtpException(e);
    } finally {
      releaseLock();
    }
  }

  @Override
  public void setKeepAliveTimeout(long seconds) throws FileTransferException {
    try {
      acquireLock();
      ftpClient().setControlKeepAliveTimeout(seconds);
    } catch (IOException e) {
      throw new FileTransferException(e);
    } finally {
      releaseLock();
    }
  }

  @Override
  public boolean isConnected() {
    try {
      acquireLock();
      return ftpClient().sendNoOp();
    } catch (IOException e) {
      return false;
    } finally {
      releaseLock();
    }
  }

  protected void handleReturnValue(boolean value) throws IOException {
    if (!value) {
      throw new IOException(ftpClient().getReplyString());
    }
  }
}
