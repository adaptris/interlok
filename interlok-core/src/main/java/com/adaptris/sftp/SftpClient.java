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

package com.adaptris.sftp;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferClientImp;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.ftp.FtpException;
import com.adaptris.util.FifoMutexLock;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.UserInfo;

/**
 * Provides SSH File Transfer Protocol implementation of FileTransferClient
 *
 * @author lchan
 * @author $Author: lchan $
 * @see FileTransferClient
 */
public class SftpClient extends FileTransferClientImp {

  public static final String NO_KERBEROS_AUTH = "publickey,keyboard-interactive,password";
  public static final String SSH_PREFERRED_AUTHENTICATIONS = "PreferredAuthentications";

  private static final String PARENT_DIR = "..";

  private static final String CURRENT_DIR = ".";

  private static final int DEFAULT_SSH_PORT = 22;

  private static final int DEFAULT_TIMEOUT = 1000 * 60 * 5;

  private String sshHost;
  private int sshPort;
  private int timeout;
  private long keepAliveTimeout = 0;

  private transient JSch jsch;
  private transient Session sftpSession;
  private transient ChannelSftp sftpChannel;
  private transient FifoMutexLock lock;
  private transient ConfigRepository configRepository = ConfigRepository.nullConfig;
  private transient Proxy proxy = null;

  private SftpClient(File knownHostsFile, ConfigBuilder configBuilder) throws SftpException {
    try {
      jsch = new JSch();
      lock = new FifoMutexLock();
      if (knownHostsFile != null) {
        jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
      }
      if (configBuilder != null) {
        configRepository = configBuilder.buildConfigRepository();
        proxy = configBuilder.buildProxy();
      }
      jsch.setConfigRepository(configRepository);
    } catch (Exception e) {
      throw SftpException.wrapException(e);
    }
  }

  /**
   * Constructor assuming the default SSH port.
   *
   * @param host the remote ssh host.
   */
  public SftpClient(String host) throws SftpException {
    this(host, DEFAULT_SSH_PORT, DEFAULT_TIMEOUT, null, null);
  }

  /**
   * Constructor assuming the default SSH port.
   *
   * @param addr the remote ssh host.
   */
  public SftpClient(InetAddress addr) throws SftpException {
    this(addr.getHostAddress(), DEFAULT_SSH_PORT, DEFAULT_TIMEOUT, null, null);
  }

  /**
   * Constructor.
   *
   * @param addr the remote ssh host.
   * @param port the ssh port.
   * @param timeout the timeout;
   */
  public SftpClient(InetAddress addr, int port, int timeout) throws SftpException {
    this(addr.getHostAddress(), port, timeout, null, null);
  }

  /**
   * Constructor.
   *
   * @param host the host
   * @param port the port
   * @param timeout the timeout;
   */
  public SftpClient(String host, int port, int timeout) throws SftpException {
    this(host, port, timeout, null, null);
  }

  /**
   * Constructor.
   *
   * @param host the host
   * @param port the port
   * @param timeout the timeout;
   * @param configBuilder any required behaviour for this client;
   */
  public SftpClient(String host, int port, int timeout, File knownHostsFile, ConfigBuilder configBuilder) throws SftpException {
    this(knownHostsFile, configBuilder);
    sshHost = host;
    sshPort = port;
    this.timeout = timeout;
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



  /**
   * 
   * @see FileTransferClient#connect(java.lang.String, java.lang.String)
   */
  @Override
  public void connect(String user, String password) throws IOException, FileTransferException {
    connect(user, new StandardUserInfo(password));
  }

  /**
   * Connect and login into an account on the SFTP server. This completes the entire login process
   *
   * @param user user name
   * @param prvKey private key as part of public/private key pair
   * @param prvKeyPwd the password for accessing the private key.
   * @throws FileTransferException if an FTP specific exception occurs
   */
  public void connect(String user, final byte[] prvKey, byte[] prvKeyPwd) throws FileTransferException {
    try {
      acquireLock();
      jsch.addIdentity(user, prvKey, null, prvKeyPwd);
      connect(user, new StandardUserInfo(null));
    }
    catch (JSchException e) {
      throw new SftpException(e);
    }
    finally {
      releaseLock();
    }

  }

  private void connect(String user, UserInfo ui) throws FileTransferException {
    try {
      sftpSession = jsch.getSession(user, sshHost, sshPort);
      if (configRepository.getConfig(sshHost) == null
          || StringUtils.isBlank(configRepository.getConfig(sshHost).getValue(SSH_PREFERRED_AUTHENTICATIONS))) {
        // No config, let's killoff #995
        sftpSession.setConfig(SSH_PREFERRED_AUTHENTICATIONS, NO_KERBEROS_AUTH);
      }
      sftpSession.setProxy(proxy);
      sftpSession.setDaemonThread(true);
      sftpSession.setUserInfo(ui);
      sftpSession.connect(timeout);
      sftpSession.setServerAliveInterval(new Long(getKeepAliveTimeout()).intValue());
      log("OPEN {}:{}", sshHost, sshPort);
      Channel c = sftpSession.openChannel("sftp");
      c.connect(timeout);
      sftpChannel = (ChannelSftp) c;
    }
    catch (JSchException e) {
      throw new SftpException(e);
    }
  }

  /**
   * 
   * @see FileTransferClient#dir(java.lang.String, boolean)
   */
  @Override
  public String[] dir(String dirname, boolean full) throws IOException, FileTransferException {
    checkConnected();
    List<String> names = new ArrayList<String>();
    try {
      acquireLock();
      String path = defaultIfBlank(dirname, CURRENT_DIR);
      log("DIR {}", path);
      Vector v = sftpChannel.ls(path);
      if (v != null) {
        for (Iterator i = v.iterator(); i.hasNext();) {
          ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) i.next();
          if (!(entry.getFilename().equals(CURRENT_DIR) || entry.getFilename().equals(PARENT_DIR))) {
            names.add(full ? entry.getLongname() : entry.getFilename());
          }
        }
      }
      Collections.sort(names);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not list files in " + dirname, e);
    }
    finally {
      releaseLock();
    }
    return names.toArray(new String[0]);
  }

  /**
   *
   * @see FileTransferClient#disconnect()
   */
  @Override
  public void disconnect() throws IOException, FileTransferException {
    try {
      acquireLock();
      log("BYE");
      sftpChannel.disconnect();
      sftpSession.disconnect();
    }
    catch (NullPointerException e) {

    }
    finally {
      releaseLock();
    }
  }

  /**
   *
   * @see FileTransferClient#put(java.io.InputStream, java.lang.String, boolean)
   */
  @Override
  public void put(InputStream srcStream, String remoteFile, boolean append) throws IOException, FileTransferException {
    try {
      checkConnected();
      log("PUT {}", remoteFile);
      sftpChannel.put(srcStream, remoteFile, append ? ChannelSftp.APPEND : ChannelSftp.OVERWRITE);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not write remote file [" + remoteFile + "]", e);
    }

  }

  /**
   *
   * @see FileTransferClient#get(java.io.OutputStream, java.lang.String)
   */
  @Override
  public void get(OutputStream destStream, String remoteFile) throws IOException, FileTransferException {
    checkConnected();
    try {
      acquireLock();
      log("GET {}", remoteFile);
      sftpChannel.get(remoteFile, destStream);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not retrieve remote file [" + remoteFile + "]", e);
    }
    finally {
      releaseLock();
    }

  }

  /**
   *
   * @see FileTransferClient#get(java.lang.String)
   */
  @Override
  public byte[] get(String remoteFile) throws IOException, FileTransferException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    get(result, remoteFile);
    return result.toByteArray();
  }

  /**
   *
   * @see FileTransferClient#delete(java.lang.String)
   */
  @Override
  public void delete(String remoteFile) throws IOException, FileTransferException {
    checkConnected();
    try {
      acquireLock();
      log("RM {}", remoteFile);
      sftpChannel.rm(remoteFile);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not delete remote file [" + remoteFile + "]", e);
    }
    finally {
      releaseLock();
    }
  }

  @Override
  public void rename(String from, String to) throws IOException, FileTransferException {
    checkConnected();
    try {
      acquireLock();
      log("REN {} to {}", from, to);
      sftpChannel.rename(from, to);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not rename file [" + from + "] to [" + to + "]", e);
    }
    finally {
      releaseLock();
    }
  }

  @Override
  public void rmdir(String dir) throws IOException, FileTransferException {
    checkConnected();
    try {
      acquireLock();
      log("RMDIR {}", dir);
      sftpChannel.rmdir(dir);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not remove directory [" + dir + "]", e);
    }
    finally {
      releaseLock();
    }
  }

  @Override
  public void mkdir(String dir) throws IOException, FileTransferException {
    checkConnected();
    try {
      acquireLock();
      log("MKDIR {}", dir);
      sftpChannel.mkdir(dir);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not create directory [" + dir + "]", e);
    }
    finally {
      releaseLock();
    }
  }

  @Override
  public void chdir(String dir) throws IOException, FileTransferException {
    checkConnected();
    try {
      acquireLock();
      log("CD {}", dir);
      sftpChannel.cd(dir);
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not chdir to [" + dir + "]", e);
    }
    finally {
      releaseLock();
    }
  }

  /**
   *
   * @see FileTransferClient#lastModifiedDate(java.lang.String)
   */
  @Override
  public Date lastModifiedDate(String remoteFile) throws IOException, FileTransferException {
    return new Date(lastModified(remoteFile));
  }

  /**
   *
   * @see FileTransferClient#lastModified(java.lang.String)
   */
  @Override
  public long lastModified(String remoteFile) throws IOException, FileTransferException {
    long mtime;
    try {
      acquireLock();
      log("STAT {}", remoteFile);
      SftpATTRS attr = sftpChannel.stat(remoteFile);
      mtime = (long) attr.getMTime() * 1000;
    }
    catch (com.jcraft.jsch.SftpException e) {
      throw new SftpException("Could not get lastModified on [" + remoteFile + "]", e);
    }
    finally {
      releaseLock();
    }
    return mtime;
  }

  private void checkConnected() throws SftpException {
    if (!isConnected()) {
      throw new SftpException("Not currently connected, use connect()");
    }
  }

  private class StandardUserInfo implements UserInfo {

    private String password;

    private StandardUserInfo(String pw) {
      password = pw;
    }

    @Override
    public String getPassword() {
      return password;
    }

    @Override
    public boolean promptYesNo(String message) {
      return true;
    }

    @Override
    public String getPassphrase() {
      return null;
    }

    @Override
    public boolean promptPassphrase(String message) {
      return true;
    }

    @Override
    public boolean promptPassword(String message) {
      return true;
    }

    @Override
    public void showMessage(String message) {
      logR.trace(message);
    }
  }

  @Override
  public long getKeepAliveTimeout() throws FtpException {
    return keepAliveTimeout;
  }

  @Override
  public void setKeepAliveTimeout(long seconds) throws FtpException {
    if (seconds > 0) {
      keepAliveTimeout = seconds * 1000;
    }
  }

  public SftpClient withKeepAliveTimeout(long seconds) throws FtpException {
    setKeepAliveTimeout(seconds);
    return this;
  }

  public SftpClient withAdditionalDebug(boolean onoff) {
    setAdditionalDebug(onoff);
    return this;
  }

  @Override
  public boolean isConnected() {
    boolean result = false;
    if (sftpChannel != null) {
      result = sftpChannel.isConnected();
    }
    return result;
  }

  public void setKnownHosts(String knownHostsFilename) throws SftpException {
    try {
      jsch.setKnownHosts(knownHostsFilename);
    }
    catch (JSchException e) {
      throw new SftpException(e);
    }
  }
}
