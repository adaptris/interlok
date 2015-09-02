package com.adaptris.core.ftp;

import java.io.File;
import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.LenientKnownHosts;
import com.adaptris.sftp.SftpClient;
import com.adaptris.sftp.SftpConnectionBehaviour;
import com.adaptris.sftp.SftpException;
import com.adaptris.sftp.StrictKnownHosts;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SFTP Connection class using public/private key authentication
 * <p>
 * This connection implementation allows you to use a public / private key pair to authenticate against the sftp server. It deviates
 * from a standard {@link SftpConnection}. Rather than specifying a default password for accessing the server, you specify a
 * {@link #setPrivateKeyFilename(String)} and {@link #setPrivateKeyPassword(String)} which contains your private key credentials
 * which are then supplied to the server.
 * </p>
 * <p>
 * It has the following behavioural changes from a standard SftpConnection :
 * <ul>
 * <li>If the private key is not accepted by the target server, then an exception will be thrown.</li>
 * <li>If no private key password is specified then it is assumed to be a 0 length string.</li>
 * <li>Only a single privatekey file will be supported per SftpKeyAuthConnection instance.</li>
 * <li>Specifying the username+password in the destination (e.g. <code>sftp://lchan:myPassword@1.2.3.4:22//opt/sftp</code>), will
 * override the username used to login but no other credentials. The only valid authentication is via the specified private key.</li>
 * <li>You can specify additional behaviour using one of {@link DefaultSftpBehaviour}, {@link LenientKnownHosts} or
 * {@link StrictKnownHosts}. {@link StrictKnownHosts} will cause an exception to be thrown if the servers key is not present in any
 * configured known_hosts file.</li>
 * <li>The private key and known_hosts file are expected to be in OpenSSH format</li>
 * </ul>
 * </p>
 * <p>
 * The password associated with {@link #setPrivateKeyPassword(String)} may be encoded using any of the standard {@link Password}
 * mechanisms and it will be decoded when the private key is first accessed.
 * </p>
 * 
 * @config sftp-key-auth-connection
 * @license BASIC
 * @author dsefton
 */
@XStreamAlias("sftp-key-auth-connection")
public class SftpKeyAuthConnection extends FileTransferConnection {

  private static final String SCHEME_SFTP = "sftp";

  static final int DEFAULT_CONTROL_PORT = 22;
  private static final int DEFAULT_TIMEOUT = 60000;

  private String privateKeyFilename;
  private String privateKeyPassword;
  @AdvancedConfig
  private Integer socketTimeout;
  @Valid
  @NotNull
  @AutoPopulated
  @AdvancedConfig
  private SftpConnectionBehaviour sftpConnectionBehaviour;
  // For sending keep alives every 60 seconds on the control port when downloading stuff.
  // Could make it configurable
  private transient long keepAlive = 60;

  public SftpKeyAuthConnection() {
    super();
    setSftpConnectionBehaviour(new DefaultSftpBehaviour());
  }


  @Override
  protected boolean acceptProtocol(String s) {
    return SCHEME_SFTP.equalsIgnoreCase(s);
  }

  @Override
  protected FileTransferClient create(String remoteHost, int port, UserInfo ui) throws IOException, FileTransferException {
    log.debug("Connecting to " + remoteHost + ":" + port + " as user " + ui.getUser());
    SftpClient sftp = new SftpClient(remoteHost, port, socketTimeout(), getSftpConnectionBehaviour());
    sftp.setAdditionalDebug(additionalDebug());
    sftp.setKeepAliveTimeout(keepAlive);
    try {
      byte[] privateKey = FileUtils.readFileToByteArray(new File(getPrivateKeyFilename()));
      sftp.connect(ui.getUser(), privateKey, Password.decode(getPrivateKeyPassword()).getBytes());
    }
    catch (PasswordException e) {
      throw new SftpException(e);
    }
    return sftp;
  }

  public Integer getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * The socket timeout in milliseconds for connect / read /write operations.
   * 
   * @param t The socketTimeout to set, default is 60000
   */
  public void setSocketTimeout(Integer t) {
    socketTimeout = t;
  }

  int socketTimeout() {
    return getSocketTimeout() != null ? getSocketTimeout().intValue() : DEFAULT_TIMEOUT;
  }

  public String getPrivateKeyFilename() {
    return privateKeyFilename;
  }

  /**
   * The name of the file where the private key is held
   *
   * @param privateKeyFilename name of file holding the private key
   */
  public void setPrivateKeyFilename(String privateKeyFilename) {
    this.privateKeyFilename = privateKeyFilename;
  }

  /**
   * The password for the private key (if it has one)
   *
   * @return private key password
   */
  public String getPrivateKeyPassword() {
    return privateKeyPassword;
  }

  /**
   * The password for the private key (if it has one)
   *
   * @param privateKeyPassword
   */
  public void setPrivateKeyPassword(String privateKeyPassword) {
    this.privateKeyPassword = privateKeyPassword;
  }

  @Override
  protected UserInfo createUserInfo() throws FileTransferException {
    return new UserInfo(getDefaultUserName());
  }

  public SftpConnectionBehaviour getSftpConnectionBehaviour() {
    return sftpConnectionBehaviour;
  }

  public void setSftpConnectionBehaviour(SftpConnectionBehaviour k) {
    if (k == null) {
      throw new IllegalArgumentException("known_hosts handler may not be null");
    }
    sftpConnectionBehaviour = k;
  }

  @Override
  public int defaultControlPort() {
    return getDefaultControlPort() != null ? getDefaultControlPort().intValue() : DEFAULT_CONTROL_PORT;
  }
}

