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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferClientImp;
import com.adaptris.util.FifoMutexLock;

/**
 * Base implementation of {@link FileTransferClient} that uses the apache
 * commons net FTP implementation.
 * 
 * @author D.Sefton
 * 
 */
public abstract class ApacheFtpClientImpl<T extends FTPClient> extends FileTransferClientImp implements FtpFileTransferClient {

  private static final String LINEFEED = System.getProperty("line.separator");
  private static final int CHUNK_SIZE = 4096;

  private transient T ftp;
  private transient TimezoneDateHandler tzHandler;

  // This is to handle possible thread safety from Usage from
  // FileTransferConnection
  private transient FifoMutexLock lock;

  private ApacheFtpClientImpl() {
    super();
    lock = new FifoMutexLock();
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
    initConnect(remoteHost, port, timeout);
    tzHandler = new TimezoneDateHandler(TimeZone.getDefault());
  }

  /**
   * This is the actual connect process
   */
  private void initConnect(String remoteHost, int port, int timeout) throws IOException {
    ftp = createFTPClient();
    ftp.setConnectTimeout(timeout);

    try {
      ftp.connect(remoteHost, port);
      logReply(ftp.getReplyStrings());
      int replyCode = ftp.getReplyCode();
      if (!FTPReply.isPositiveCompletion(replyCode)) {
        throw new IOException("FTP Server refused connection");
      }
      additionalSettings(ftp);
    }
    catch (IOException e) {
      if (ftp.isConnected()) {
        ftp.disconnect();
      }
      throw e;
    }
  }

  /**
   * Create the base commons net client.
   * 
   * @return the actual FtpClient implementation that will be used.
   */
  protected abstract T createFTPClient();

  protected abstract void additionalSettings(T client) throws IOException;

  /**
   * The timeout period once a connection has been opened
   */
  public int getTimeout() {
    return ftp.getConnectTimeout();
  }

  /**
   * The timeout period once a connection has been opened
   * 
   * @param millis
   */
  public void setTimeout(int millis) {
    ftp.setConnectTimeout(millis);
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
      handleReturnValue(ftp.login(user, password));
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      handleReturnValue(ftp.login(user, password, account));
    }
    finally {
      logReply(ftp.getReplyStrings());
      releaseLock();
    }
  }

  @Override
  public void put(InputStream srcStream, String remoteFile, boolean append) throws IOException {
    try {
      acquireLock();
      if (append) {
        log("{} {}", FTPCmd.APPE, remoteFile);
        handleReturnValue(ftp.appendFile(remoteFile, srcStream));
      }
      else {
        log("{} {}", FTPCmd.STOR, remoteFile);
        handleReturnValue(ftp.storeFile(remoteFile, srcStream));
      }
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      handleReturnValue(ftp.retrieveFile(remoteFile, destStream));
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      // Get input stream from FTP server
      BufferedInputStream in = new BufferedInputStream(ftp.retrieveFileStream(remoteFile));
      long size = 0;
      byte[] chunk = new byte[CHUNK_SIZE];
      int count;

      // Read from input and write to output in chunks
      try {
        while ((count = in.read(chunk, 0, CHUNK_SIZE)) >= 0) {
          out.write(chunk, 0, count);
          size += count;
        }

        ftp.completePendingCommand();
      }
      finally {
        out.flush();

        in.close();
      }
      logReply(ftp.getReplyStrings());
      log("Transferred " + size + " bytes from remote host");
    }
    finally {
      releaseLock();
    }
    return out.toByteArray();
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
      FTPFile[] results = ftp.listFiles(dirname);
      logReply(ftp.getReplyStrings());
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
      handleReturnValue(ftp.deleteFile(remoteFile));
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      handleReturnValue(ftp.rename(from, to));
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      handleReturnValue(ftp.removeDirectory(dir));
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      handleReturnValue(ftp.makeDirectory(dir));
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      handleReturnValue(ftp.changeWorkingDirectory(dir));
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      ftp.disconnect();
    }
    finally {
      logReply(ftp.getReplyStrings());
      releaseLock();
    }
  }

  /**
   * Set transfer type eg. ASCII, BINARY
   * 
   * @param ftpFileType FILE_TYPE constant from the Apache commons net FtpClient
   *          class
   */
  public void setType(TransferType ftpFileType) throws IOException {
    ftpFileType.applyTransferType(ftp);
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
      ftp.sendCommand("MDTM", path);
      Reply reply = validateReply(ftp.getReplyString(), "213");
      lastModified = tzHandler.asLong(reply.getReplyText());
    }
    finally {
      logReply(ftp.getReplyStrings());
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
      ftp.sendCommand("MDTM", path);
      Reply reply = validateReply(ftp.getReplyString(), "213");
      lastModified = tzHandler.asDate(reply.getReplyText());
    }
    finally {
      logReply(ftp.getReplyStrings());
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

  /**
   * Switch debug of responses on or off
   * 
   * @param on true if you wish to have responses to stdout, false otherwise
   * @deprecated since 3.0.4 use {@link #setAdditionalDebug(boolean)} instead.
   */
  @Deprecated
  public void debugResponses(boolean on) {
    setAdditionalDebug(on);
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
          sb.append(System.getProperty("line.separator"));
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
      ftp.syst();
      result = ftp.getReplyString();
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
      ftp.pwd();
      result = ftp.getReplyString();
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

  public void setDataMode(FtpDataMode mode) {
    try {
      acquireLock();
      mode.applyDataMode(ftp);
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
  private Reply validateReply(String reply, String expectedReplyCode) throws IOException, FtpException {

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

  /**
   * Get the time to wait between sending control connection keepalive messages.
   * 
   * @return time in seconds
   * @author Daniel Sefton
   */
  @Override
  public long getKeepAliveTimeout() throws FtpException {
    return ftp.getControlKeepAliveTimeout();
  }

  /**
   * Set the time to wait between sending control connection keepalive messages
   * when processing file upload or download.
   * 
   * @param seconds time in seconds
   */
  @Override
  public void setKeepAliveTimeout(long seconds) throws FtpException {
    ftp.setControlKeepAliveTimeout(seconds);
  }

  @Override
  public boolean isConnected() {
    try {
      return ftp.sendNoOp();
    }
    catch (IOException e) {
      return false;
    }
  }

  void handleReturnValue(boolean value) throws IOException {
    if (!value) {
      throw new IOException(ftp.getReplyString());
    }
  }
}
