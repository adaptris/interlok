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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.NoOpConnection;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.ftp.FtpException;
import com.adaptris.security.password.Password;

/**
 * Class containing common configuration for all FTP Connection types.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class FileTransferConnection extends NoOpConnection {
  /**
   * The default size of the cache if a size isn't specified ({@value #DEFAULT_MAX_CACHE_SIZE})
   *
   * @see #setMaxClientCache(Integer)
   */
  protected static final int DEFAULT_MAX_CACHE_SIZE = 16;
  private static final String UTF_8 = "UTF-8";

  private String defaultUserName;
  @AdvancedConfig
  private Integer defaultControlPort;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean forceRelativePath;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean additionalDebug;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean windowsWorkAround;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean cacheConnection;
  @AdvancedConfig
  private Integer maxClientCacheSize;


  private transient SimpleCache cachedConnections;

  /**
   *
   */
  public FileTransferConnection() {
  }

  /**
   * Force the path to be relative when using {@linkplain #getDirectoryRoot(String)}.
   * <p>
   * This is useful in the situation where the server in question does not have the user in a ftp jail
   * </p>
   *
   * @param b true to prefix a <code>.</code> to the path.
   * @see #getDirectoryRoot(String)
   */
  public void setForceRelativePath(Boolean b) {
    forceRelativePath = b;
  }

  /**
   * Get the force relative path flag.
   *
   * @return true or false.
   * @see #setForceRelativePath(Boolean)
   */
  public Boolean getForceRelativePath() {
    return forceRelativePath;
  }

  public boolean forceRelativePath() {
    return getForceRelativePath() != null ? getForceRelativePath().booleanValue() : false;
  }

  /**
   * <p>
   * Returns the default user name.
   * </p>
   *
   * @return the default user name
   */
  public String getDefaultUserName() {
    return defaultUserName;
  }

  /**
   * Set the user name.
   *
   * @param s the username.
   */
  public void setDefaultUserName(String s) {
    defaultUserName = s;
  }

  /**
   * @return Returns the defaultControlPort.
   */
  public Integer getDefaultControlPort() {
    return defaultControlPort;
  }

  public abstract int defaultControlPort();
  
  /**
   * Override the default port.
   *
   * @param i The defaultControlPort to set.
   */
  public void setDefaultControlPort(Integer i) {
    defaultControlPort = i;
  }

  /**
   * Get additional logging output where available.
   *
   * @param b true to get additional logging.
   */
  public void setAdditionalDebug(Boolean b) {
    additionalDebug = b;
  }

  /**
   * The additional debug flag.
   *
   * @see #setAdditionalDebug(Boolean)
   * @return true or false (default false)
   */
  public Boolean getAdditionalDebug() {
    return additionalDebug;
  }

  public boolean additionalDebug() {
    return getAdditionalDebug() != null ? getAdditionalDebug().booleanValue() : false;
  }

  /**
   * @return the windowsWorkAround
   * @see #setWindowsWorkAround(Boolean)
   */
  public Boolean getWindowsWorkAround() {
    return windowsWorkAround;
  }

  /**
   * Set whether the target server is a windows machine that returns backslash separated filenames when doing NLIST on a directory
   * rather than the normal forward slashes.
   *
   * @param b the windowsWorkAround to set
   */
  public void setWindowsWorkAround(Boolean b) {
    windowsWorkAround = b;
  }

  public boolean windowsWorkaround() {
    return getWindowsWorkAround() != null ? getWindowsWorkAround().booleanValue() : false;
  }

  /**
   * @return whether connection is held open after use
   */
  public Boolean getCacheConnection() {
    return cacheConnection;
  }

  /**
   * Set whether or not connections created are held open for future use.
   * <p>
   * This feature is primarily intended to mitigate the connection cost when using {@linkplain FtpConsumer} with a frequent poll
   * interval.If multiple components end up using the same {@linkplain FileTransferClient} instance from the cache (perhaps you have
   * multiple FtpConsumer instances configured in the same channel) then you will likely end up with non-optimal performance due to
   * thread synchronisation.
   * </p>
   * <p>
   * It is generally recommended that you configure the associated {@linkplain FtpConsumer} and {@linkplain FtpProducer} instances
   * using the URL form of the destination if you intend to use the caching feature; this allows you to make sure that each
   * component has its own unique FileTransferClient instance associated with it.
   * </p>
   *
   * @param b true to enable, default false.
   * @see #setMaxClientCache(Integer)
   */
  public void setCacheConnection(Boolean b) {
    cacheConnection = b;
  }

  public boolean cacheConnection() {
    return getCacheConnection() != null ? getCacheConnection().booleanValue() : false;
  }

  /**
   *
   * @see com.adaptris.core.NullConnection#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    if (defaultUserName == null) {
      log.warn("No default user name, expected to be provided by destination");
    }
    cachedConnections = new SimpleCache();
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
   */
  @Override
  protected void closeConnection() {
    for (FileTransferClient client : cachedConnections.values()) {
      forceDisconnect(client);
    }
    cachedConnections.clear();
  }

  /**
   * Connect to the host.
   *
   * @param hostUrl the host to connect to which can be in the form of an url or simply just the hostname in which case the default
   *          credentials and port numbers are used.
   * @return an FtpClient that is ready to use.
   * @throws FtpException for FTP specific errors.
   */
  public FileTransferClient connect(String hostUrl) throws FileTransferException, IOException {
    FileTransferClient client = lookup(hostUrl);
    if (client == null) {
      client = create(hostUrl);
    }
    addToCache(hostUrl, client);
    return client;
  }

  private FileTransferClient create(String hostUrl) throws FileTransferException, IOException {
    String remoteHost = hostUrl;
    UserInfo ui = createUserInfo();
    int port = defaultControlPort();
    try {
      URI uri = new URI(hostUrl);
      if (acceptProtocol(uri.getScheme())) {
        remoteHost = uri.getHost();
        port = uri.getPort() != -1 ? uri.getPort() : defaultControlPort();
        ui.parse(uri.getRawUserInfo());
      }
    }
    catch (URISyntaxException e) {
      ;
    }
    FileTransferClient client = create(remoteHost, port, ui);
    return client;
  }

  private FileTransferClient lookup(String hostUrl) {
    FileTransferClient result = null;
    if (cacheConnection()) {
      result = cachedConnections.get(hostUrl);
      if (result != null && result.isConnected()) {
        log.trace("Reusing an existing FileTransferClient for " + hostUrl);
      }
      else {
        result = null;
      }
    }
    return result;
  }

  private void addToCache(String hostUrl, FileTransferClient client) {
    if (cacheConnection()) {
      cachedConnections.put(hostUrl, client);
    }
  }

  /**
   * Get the max number of entries in the cache.
   *
   * @return the maximum number of entries.
   */
  public Integer getMaxClientCacheSize() {
    return maxClientCacheSize;
  }

  /**
   * Set the max number of entries in the cache.
   * <p>
   * Entries will be removed on a least recently accessed basis.
   * </p>
   * 
   * @param maxSize the maximum number of entries, default is {@value #DEFAULT_MAX_CACHE_SIZE}
   * @see #setCacheConnection(Boolean)
   */
  public void setMaxClientCache(Integer maxSize) {
    maxClientCacheSize = maxSize;
  }

  public int maxClientCacheSize() {
    return getMaxClientCacheSize() != null ? getMaxClientCacheSize().intValue() : DEFAULT_MAX_CACHE_SIZE;
  }

  /**
   * Validate the URL Protocol when a URL is used.
   *
   * @param s the URL Protocol
   * @return true if the URL protocol is acceptable to the concrete imp.
   */
  protected abstract boolean acceptProtocol(String s);

  /**
   * Create an instance of the <code>FileTransferClient</code> for use with the producer or consumer.
   *
   * @param host the remote host.
   * @param port the port to connect to
   * @param ui a local UserInfo containing username and password
   * @return a <code>FileTransferClient</code> object
   * @throws IOException wrapping a general comms error.
   * @throws FileTransferException if a protocol specific exception occurred.
   */
  protected abstract FileTransferClient create(String host, int port, UserInfo ui) throws IOException, FileTransferException;

  /**
   * <p>
   * Returns the directory root for the passed host URL.
   * </p>
   *
   * @param hostUrl the host URL
   * @return the directory root for the passed host URL
   */
  public String getDirectoryRoot(String hostUrl) {
    String result = "";
    try {
      URI uri = new URI(hostUrl);
      if (acceptProtocol(uri.getScheme())) {
        result = uri.getPath() != null ? uri.getPath() : "";
      }
    }
    catch (URISyntaxException e) {
      ;
    }
    if (forceRelativePath()) {
      result = "." + result;
    }
    return result;
  }

  /**
   * Disconnect the FTP client.
   *
   * @param ftp the ftp client implementation
   */
  public void disconnect(FileTransferClient ftp) {
    if (!cacheConnection()) {
      forceDisconnect(ftp);
    }
  }

  private void forceDisconnect(FileTransferClient ftp) {
    try {
      if (ftp != null) {
        ftp.disconnect();
      }
    }
    catch (Exception e) {
      log.warn("Can not execute the FTP quit command " + e.getMessage());
    }
  }

  protected abstract UserInfo createUserInfo() throws FileTransferException;

  protected class UserInfo {
    private String user, password;

    protected UserInfo() {
    }

    protected UserInfo(String defaultUser) {
      user = defaultUser;
    }

    protected UserInfo(String defaultUser, String defaultPassword) throws FileTransferException {
      try {
        user = defaultUser;
        password = Password.decode(defaultPassword);
      }
      catch (Exception e) {
        throw new FileTransferException(e);
      }
    }

    protected void parse(String fulluserpass) throws UnsupportedEncodingException {
      if (isEmpty(fulluserpass)) {
        return;
      }
      // at this point we know that there are no invalid chars, because we used getRawUserInfo()
      // But we want to use raw, because they might be pesky and have : in their password.
      int passindex = fulluserpass.indexOf(':');
      if (passindex != -1) {
        user = URLDecoder.decode(fulluserpass.substring(0, passindex), UTF_8);
        password = URLDecoder.decode(fulluserpass.substring(passindex + 1), UTF_8);
      }
      else {
        user = URLDecoder.decode(fulluserpass, UTF_8);
      }
    }

    public String getUser() {
      return user;
    }

    public String getPassword() {
      return password;
    }
  }

  private class SimpleCache extends LinkedHashMap<String, FileTransferClient> {

    private static final long serialVersionUID = 2011031601L;

    public SimpleCache() {
      super(maxClientCacheSize(), 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, FileTransferClient> eldest) {
      boolean result = size() > maxClientCacheSize();
      if (result) {
        forceDisconnect(eldest.getValue());
      }
      return result;
    }
  }

}
