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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.util.TimeZone;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.ftp.ApacheFtpClientImpl;
import com.adaptris.ftp.FtpDataMode;
import com.adaptris.ftp.TransferType;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.NumberUtils;

/**
 * Abstract implementation of FTPConnection both vanilla and SSL.
 * 
 * @author lchan
 * 
 */
public abstract class FtpConnectionImp extends FileTransferConnectionUsingPassword {

  private static final int DEFAULT_SOCKET_TIMEOUT = 60000;
  private static final FtpDataMode DEFAULT_DATA_MODE = FtpDataMode.PASSIVE;
  private static final TransferType DEFAULT_TRANSFER_TYPE = TransferType.BINARY;
  static final int DEFAULT_FTP_CONTROL_PORT = 21;

  @AutoPopulated
  private TransferType transferType;
  @AutoPopulated
  @AdvancedConfig
  private Integer socketTimeout;
  @AdvancedConfig
  private String serverTimezone;
  @AutoPopulated
  private FtpDataMode ftpDataMode;
  @AdvancedConfig
  private String defaultAccount;
  @AdvancedConfig
  @NotNull
  @AutoPopulated
  private KeyValuePairSet additionalSettings;

  // For sending keep alives every 60 seconds on the control port when downloading stuff.
  // Could make it configurable
  private transient int controlKeepAlive = 60;



  /**
   * Default Constructor with the following default values.
   * <ul>
   * <li>ftpDebug is false</li>
   * <li>defaultControlPort is 21</li>
   * </ul>
   * 
   * @see FileTransferConnection#FileTransferConnection()
   */
  public FtpConnectionImp() {
    super();
    setAdditionalSettings(new KeyValuePairSet());
  }

  TransferType transferType() {
    return getTransferType() != null ? getTransferType() : DEFAULT_TRANSFER_TYPE;
  }

  /**
   * Get the transfer type.
   * 
   * @return the transfer type.
   * @see TransferType
   */
  public TransferType getTransferType() {
    return transferType;
  }

  /**
   * Set the transfer type.
   * 
   * @param s the transfer type, default is {@link TransferType#BINARY}
   * @see TransferType
   */
  public void setTransferType(TransferType s) {
    transferType = s;
  }

  /**
   * @return Returns the socketTimeout.
   */
  public Integer getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * The socket timeout in milliseconds for connect / read and write operations.
   * 
   * @param i The socketTimeout to set; default is 60000
   */
  public void setSocketTimeout(Integer i) {
    socketTimeout = i;
  }

  int socketTimeout() {
    return NumberUtils.toIntDefaultIfNull(getSocketTimeout(), DEFAULT_SOCKET_TIMEOUT);
  }

  /**
   * @return the serverTimezone
   */
  public String getServerTimezone() {
    return serverTimezone;
  }

  /**
   * Specify the timezone which the server is in.
   * <p>
   * If not explicitly specified then the server Timezone will be assumed to be the same as the client timezone. This might lead to
   * incorrect modification timestamps.
   * </p>
   * 
   * @param tz any valid java timezone ID
   * @see java.util.TimeZone#getTimeZone(String)
   */
  public void setServerTimezone(String tz) {
    serverTimezone = tz;
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisConnectionImp#init()
   */
  @Override
  protected void initConnection() throws CoreException {
    super.initConnection();
  }

  protected abstract ApacheFtpClientImpl createFtpClient(String remoteHost, int port, int seconds) throws IOException;

  @Override
  protected FileTransferClient create(String remoteHost, int port, UserInfo ui)
      throws IOException, FileTransferException, PasswordException {
    log.debug("Connecting to {}:{} as user {}", remoteHost, port, ui.getUser());

    ApacheFtpClientImpl ftp = createFtpClient(remoteHost, port, socketTimeout())
        .withAdditionalSettings(KeyValuePairBag.asMap(getAdditionalSettings()));
    ftp.setDataMode(ftpDataMode());
    ftp.setKeepAliveTimeout(controlKeepAlive);
    if (getServerTimezone() != null) {
      ftp.setServerTimezone(TimeZone.getTimeZone(getServerTimezone()));
    }
    ftp.setAdditionalDebug(additionalDebug());
    if (!isEmpty(accountName())) {
      ftp.connect(ui.getUser(), Password.decode(ui.getPassword()), accountName());
    }
    else {
      ftp.connect(ui.getUser(), Password.decode(ui.getPassword()));
    }
    ftp.setType(transferType());
    if (additionalDebug()) {
      log.trace("Server OS [{}], Current Directory [{}], Transfer Type [{}]",ftp.system(),ftp.pwd(),transferType().name());
    }
    return ftp;
  }

  FtpDataMode ftpDataMode() {
    return getFtpDataMode() != null ? getFtpDataMode() : DEFAULT_DATA_MODE;
  }

  public FtpDataMode getFtpDataMode() {
    return ftpDataMode;
  }

  /**
   * Set the FTP Data Mode.
   * <p>
   * The following chart should help you remember how each FTP mode works:
   * 
   * <pre>
   * {@code 
     Active FTP :
       command : client >1023 -> server 21
       data    : client >1023 <- server 20

     Passive FTP :
       command : client >1023 -> server 21
       data    : client >1023 -> server >1023
     }
     </pre>
   * </p>
   * <p>
   * Active FTP is beneficial to the FTP server admin, but detrimental to the client side admin. The FTP server attempts to make
   * connections to random high ports on the client, this may not work for the following reasons
   * <ul>
   * <li>The client has a firewall</li>
   * <li>The client is connected in conjunction with a NAT device</li>
   * </ul>
   * </p>
   * <p>
   * Passive FTP is beneficial to the client, but might be considered detrimental to the FTP server admin. The client will make both
   * connections to the server, but one of them will be to a random high port, which would almost certainly be blocked by a firewall
   * on the server side.
   * </p>
   * <p>
   * Since admins running FTP servers generally need to make their servers accessible to the greatest number of clients, they will
   * almost certainly need to support passive FTP. Perhaps the FTP Server may be configured to use a limited range of ports for
   * passive transfers (e.g. the ProFTPd PassivePorts directive) which can be added to the firewalls allowed list.
   * </p>
   * 
   * @param s either ACTIVE or PASSIVE, default is {@link FtpDataMode#PASSIVE}
   * @see FtpDataMode
   */
  public void setFtpDataMode(FtpDataMode s) {
    ftpDataMode = s;
  }

  /**
   * @return the defaultAccount
   */
  public String getDefaultAccount() {
    return defaultAccount;
  }

  /**
   * Some FTP Servers force you to have an account which is separate from the username.
   * 
   * <p>
   * Note that if a positive completion code is returned by the server when you submit the password, then the account is never
   * submitted; so you can still share the same FtpConnection between multiple consumers provided that you have the same account
   * name (or no account name) for each of the connections.
   * </p>
   * 
   * @param defaultAccount the defaultAccount to set
   */
  public void setDefaultAccount(String defaultAccount) {
    this.defaultAccount = defaultAccount;
  }

  private String accountName() {
    return getDefaultAccount();
  }

  @Override
  public int defaultControlPort() {
    return NumberUtils.toIntDefaultIfNull(getDefaultControlPort(), DEFAULT_FTP_CONTROL_PORT);
  }

  public KeyValuePairSet getAdditionalSettings() {
    return additionalSettings;
  }

  /**
   * Set any additional settings that might need to be applied.
   * 
   * @param s any additional settings.
   * @see com.adaptris.ftp.ClientSettings.FTP
   * @see com.adaptris.ftp.ClientSettings.FTPS
   */
  public void setAdditionalSettings(KeyValuePairSet s) {
    this.additionalSettings = Args.notNull(s, "additionalSettings");
  }
}
