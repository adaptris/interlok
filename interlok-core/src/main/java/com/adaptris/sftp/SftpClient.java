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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferClientImp;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.ftp.FtpException;
import com.adaptris.interlok.cloud.RemoteFile;
import com.adaptris.util.FifoMutexLock;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.UserInfo;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  /**
   * Whether not extended debugging is emitted; defaults to false unless explicitly set via either the system property
   * {@code interlok.jsch.debug}.
   *
   */
  public static final transient boolean JSCH_DEBUG = Boolean.getBoolean("interlok.jsch.debug");

  private String sshHost;
  private int sshPort;
  private int timeout;
  private long keepAliveTimeout = 0;

  private transient JSch jsch;
  private transient FifoMutexLock lock;
  private transient ConfigRepository configRepository = ConfigRepository.nullConfig;
  private transient Proxy proxy = null;
  private transient SessionWrapper sessionWrapper;

  static {
    if (JSCH_DEBUG) {
      JSch.setLogger(new SftpClientLogger());
    }
  }

  private SftpClient(File knownHostsFile, ConfigBuilder configBuilder) throws SftpException {
    try {
      jsch = new JSch();
      lock = new FifoMutexLock();
      if (knownHostsFile != null) {
        setKnownHosts(knownHostsFile.getAbsolutePath());
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
  @Generated
  public SftpClient(String host) throws SftpException {
    this(host, DEFAULT_SSH_PORT);
  }

  /**
   * Constructor assuming the default SSH port.
   *
   * @param host the remote ssh host.
   * @param port the ssh port.
   */
  @Generated
  public SftpClient(String host, int port) throws SftpException {
    this(host, port, DEFAULT_TIMEOUT);
  }

  /**
   * Constructor assuming the default SSH port.
   *
   * @param addr the remote ssh host.
   * @deprecated since 4.5.0
   */
  @Deprecated
  @Generated
  public SftpClient(InetAddress addr) throws SftpException {
    this(addr.getHostAddress(), DEFAULT_SSH_PORT, DEFAULT_TIMEOUT, null, null);
  }

  /**
   * Constructor.
   *
   * @param addr the remote ssh host.
   * @param port the ssh port.
   * @param timeoutMillis the timeout in milliseconds
   * @deprecated since 4.5.0
   */
  @Deprecated
  @Generated
  public SftpClient(InetAddress addr, int port, int timeoutMillis) throws SftpException {
    this(addr.getHostAddress(), port, timeoutMillis, null, null);
  }

  /**
   * Constructor.
   *
   * @param host the host
   * @param port the port
   * @param timeoutMillis the timeout in milliseconds
   */
  @Generated
  public SftpClient(String host, int port, int timeoutMillis) throws SftpException {
    this(host, port, timeoutMillis, null, null);
  }

  /**
   * Constructor.
   *
   * @param host the host
   * @param port the port
   * @param timeoutMillis the timeout in milliseconds
   * @param knownHostsFile the {@code 'known_hosts'} which can be null
   * @param configBuilder any required behaviour for this client;
   */
  public SftpClient(String host, int port, int timeoutMillis, File knownHostsFile, ConfigBuilder configBuilder) throws SftpException {
    this(knownHostsFile, configBuilder);
    sshHost = host;
    sshPort = port;
    this.timeout = timeoutMillis;
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
    sessionWrapper = tryConnect(new PasswordWrapper(user,  password), new KeyboardInteractiveWrapper(user, password));
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
    sessionWrapper = tryConnect(new PublickeyWrapper(user,  prvKey,  prvKeyPwd));
  }

  private SessionWrapper tryConnect(SessionWrapper... wrappers) throws SftpException {
    Exception lastAuthFailure = new Exception("Failed to authenticate via any permitted method");
    try {
      for (SessionWrapper wrapper : wrappers) {
        try {
          wrapper.connect();
          return wrapper;
        } catch (Exception e) {
          lastAuthFailure = e;
        }
      }
    } finally {
      releaseLock();
    }
    throw SftpException.wrapException(lastAuthFailure);
  }

  @Override
  public String[] dir(String directory, FileFilter filter) throws FileTransferException, IOException {
    List<File> files = toFileList(listFiles(directory));
    List<String> output = new ArrayList<String>();
    FileFilter filterToUse = ensureNotNull(filter);
    for (File file : files) {
      if (filterToUse.accept(file)) {
        output.add(file.getName());
      }
    }
    return output.toArray(new String[0]);
  }

  /**
   *
   * @see FileTransferClient#dir(java.lang.String, boolean)
   */
  @Override
  public String[] dir(String dirname, boolean full) throws IOException, FileTransferException {
    List<String> names = new ArrayList<String>();
    List<ChannelSftp.LsEntry> files = listFiles(dirname);
    for (ChannelSftp.LsEntry entry : files) {
      names.add(full ? entry.getLongname() : entry.getFilename());
    }
    Collections.sort(names);
    return names.toArray(new String[0]);
  }

  private List<File> toFileList(List<ChannelSftp.LsEntry> files) {
    ArrayList<File> result = new ArrayList<>();
    for (ChannelSftp.LsEntry f : files) {
      long lastModified = f.getAttrs().getMTime() * 1000L;
      long size = f.getAttrs().getSize();
      boolean isDir = f.getAttrs().isDir();
      // Is a regular file a file or could it be both a file & a directory.
      // Unix knowledge is poor.
      boolean isFile = f.getAttrs().isReg();
      result.add(new RemoteFile.Builder().setPath(f.getFilename()).setIsDirectory(isDir).setIsFile(isFile)
          .setLastModified(lastModified)
              .setLength(size).build());
    }
    return result;
  }


  private List<ChannelSftp.LsEntry> listFiles(String dirname) throws IOException, FileTransferException {
    checkConnected();
    List<ChannelSftp.LsEntry> result = new ArrayList<>();
    try {
      acquireLock();
      String path = defaultIfBlank(dirname, CURRENT_DIR);
      log("DIR {}", path);
      Vector<LsEntry> v = sessionWrapper.getSftpChannel().ls(path);
      for (LsEntry entry : v) {
        if (!(entry.getFilename().equals(CURRENT_DIR) || entry.getFilename().equals(PARENT_DIR))) {
          result.add(entry);
        }
      }
    } catch (Exception e) {
      throw SftpException.wrapException("Could not list files in " + dirname, e);
    } finally {
      releaseLock();
    }
    return result;
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
      sessionWrapper.close();
    }
    catch (NullPointerException e) {

    }
    finally {
      sessionWrapper = null;
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
      sessionWrapper.getSftpChannel().put(srcStream, remoteFile, append ? ChannelSftp.APPEND : ChannelSftp.OVERWRITE);
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not write remote file [" + remoteFile + "]", e);
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
      sessionWrapper.getSftpChannel().get(remoteFile, destStream);
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not retrieve remote file [" + remoteFile + "]", e);
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
      sessionWrapper.getSftpChannel().rm(remoteFile);
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not delete remote file [" + remoteFile + "]", e);
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
      sessionWrapper.getSftpChannel().rename(from, to);
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not rename file [" + from + "] to [" + to + "]", e);
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
      sessionWrapper.getSftpChannel().rmdir(dir);
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not remove directory [" + dir + "]", e);
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
      sessionWrapper.getSftpChannel().mkdir(dir);
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not create directory [" + dir + "]", e);
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
      sessionWrapper.getSftpChannel().cd(dir);
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not chdir to [" + dir + "]", e);
    }
    finally {
      releaseLock();
    }
  }

  @Override
  public boolean isDirectory(String path) throws IOException {
    checkConnected();
    try {
      acquireLock();
      log("STAT {}", path);
      SftpATTRS attrs = sessionWrapper.getSftpChannel().stat(path);
      return attrs.isDir();
    } catch (Exception e) {
      throw SftpException.wrapException("Could not stat [" + path + "]", e);
    } finally {
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
    checkConnected();
    try {
      acquireLock();
      log("STAT {}", remoteFile);
      SftpATTRS attr = sessionWrapper.getSftpChannel().stat(remoteFile);
      mtime = (long) attr.getMTime() * 1000;
    }
    catch (Exception e) {
      throw SftpException.wrapException("Could not get lastModified on [" + remoteFile + "]", e);
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
    return Optional.ofNullable(sessionWrapper).map((s) -> s.connected()).orElse(false);
  }

  public void setKnownHosts(String knownHostsFilename) throws SftpException {
    try {
      jsch.setKnownHosts(knownHostsFilename);
    }
    catch (Exception e) {
      throw SftpException.wrapException(e);
    }
  }

  private interface SessionWrapper {
    void connect() throws Exception;
    void close();
    ChannelSftp getSftpChannel();
    boolean connected();
  }

  @NoArgsConstructor
  private abstract class SessionWrapperImpl implements SessionWrapper {
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private transient String username;
    @Getter(AccessLevel.PROTECTED)
    private transient Session sftpSession;
    @Getter
    private transient ChannelSftp sftpChannel;

    private Session createSession() throws Exception {
      Session s = jsch.getSession(getUsername(), sshHost, sshPort);
      if (configRepository.getConfig(sshHost) == null
          || StringUtils.isBlank(configRepository.getConfig(sshHost).getValue(SSH_PREFERRED_AUTHENTICATIONS))) {
        // No config, let's killoff #995
        s.setConfig(SSH_PREFERRED_AUTHENTICATIONS, NO_KERBEROS_AUTH);
      }
      s.setProxy(proxy);
      s.setDaemonThread(true);
      return s;
    }

    @Override
    public void connect() throws Exception {
      sftpSession = configureAuth(createSession());
      sftpSession.connect(timeout);
      sftpSession.setServerAliveInterval(Long.valueOf(getKeepAliveTimeout()).intValue());
      log("OPEN {}:{}", sshHost, sshPort);
      Channel c = sftpSession.openChannel("sftp");
      c.connect(timeout);
      sftpChannel = (ChannelSftp) c;
    }

    protected abstract Session configureAuth(Session s) throws Exception;

    @Override
    public void close() {
      Optional.ofNullable(getSftpChannel()).ifPresent((c) -> c.disconnect());
      Optional.ofNullable(getSftpSession()).ifPresent((c) -> c.disconnect());
    }

    @Override
    public boolean connected() {
      return Optional.ofNullable(sftpChannel).map((s) -> s.isConnected()).orElse(false);
    }
  }

  // the password part of 'PreferredAuthentications=publickey,keyboard-interactive,password'
  // "PasswordAuthentication" is a built-in method of using a password.
  // RFC-4252
  private class PasswordWrapper extends SessionWrapperImpl {

    private transient UserInfo userInfo;

    PasswordWrapper(String user, String pw) {
      userInfo = new StandardUserInfo(pw);
      setUsername(user);
    }

    @Override
    protected Session configureAuth(Session s) throws Exception {
      s.setUserInfo(userInfo);
      return s;
    }
  }

  // the keyboard-interactive part of 'PreferredAuthentications=publickey,keyboard-interactive,password'
  // "ChallengeResponseAuthentication" is a method to tunnel the authentication process.
  // It just so happens that if you don't do extra configuration ChallengeResponseAuth just acts
  // like PasswordAuthentation (because of PAM configuration on Linux)
  // This is a bit of a shim in the sense that we wouldn't normally expect it to work if
  // the server is *configured properly*...
  // RFC-4252
  private class KeyboardInteractiveWrapper extends SessionWrapperImpl {

    private transient String password;

    KeyboardInteractiveWrapper(String user, String pw) {
      password = pw;
      setUsername(user);
    }

    @Override
    protected Session configureAuth(Session s) throws Exception {
      // Check com.jcraft.jsch.UserAuthKeyboardInteractive for more details.
      // We need to bypass this check
      // if(userinfo!=null && !(userinfo instanceof UIKeyboardInteractive)){
      // return false;
      // } which allows us to proceed to the next step.
      // we won't use UIKeyboardInteractive since we aren't truly supporting CRA
      s.setPassword(password);
      return s;
    }
  }

  // the publickey part of 'PreferredAuthentications=publickey,keyboard-interactive,password'
  private class PublickeyWrapper extends SessionWrapperImpl {

    private transient byte[] privateKey;
    private transient byte[] privateKeyPassword;

    PublickeyWrapper(String user, byte[] prvKey, byte[] prvKeyPwd) {
      setUsername(user);
      privateKey = prvKey;
      privateKeyPassword = prvKeyPwd;
    }

    @Override
    protected Session configureAuth(Session s) throws Exception {
      jsch.addIdentity(getUsername(), privateKey, null, privateKeyPassword);
      return s;
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

}
