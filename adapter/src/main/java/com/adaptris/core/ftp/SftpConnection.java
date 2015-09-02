package com.adaptris.core.ftp;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.SftpClient;
import com.adaptris.sftp.SftpConnectionBehaviour;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SFTP Connection class.
 * <p>
 * Handles URLs in the form <code>sftp://[user:pw]@host[:port]/path/to/directory/root</code>
 * </p>
 * 
 * @config sftp-connection
 * @license BASIC
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("sftp-connection")
public class SftpConnection extends FileTransferConnectionUsingPassword {

  private static final String SCHEME_SFTP = "sftp";

  static final int DEFAULT_CONTROL_PORT = 22;
  private static final int DEFAULT_TIMEOUT = 60000;

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

  /**
   * Default Constructor with the following default values.
   * <ul>
   * <li>socketTimeout is 0</li>
   * </ul>
   * 
   * @see FileTransferConnection#FileTransferConnection()
   */
  public SftpConnection() {
    super();
    setSftpConnectionBehaviour(new DefaultSftpBehaviour());
  }



  @Override
  protected boolean acceptProtocol(String s) {
    return SCHEME_SFTP.equalsIgnoreCase(s);
  }

  @Override
  protected FileTransferClient create(String remoteHost, int port, UserInfo ui)
      throws IOException, FileTransferException {
    log.debug("Connecting to " + remoteHost + ":" + port + " as user "
        + ui.getUser());
    SftpClient sftp = new SftpClient(remoteHost, port, socketTimeout(), getSftpConnectionBehaviour());
    sftp.setAdditionalDebug(additionalDebug());
    sftp.setKeepAliveTimeout(keepAlive);
    sftp.connect(ui.getUser(), ui.getPassword());
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
