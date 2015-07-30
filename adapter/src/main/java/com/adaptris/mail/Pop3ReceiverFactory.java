package com.adaptris.mail;

import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.mail.URLName;

import org.apache.commons.net.pop3.POP3Client;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MailReceiverFactory} that supports POP3 using the commons net {@link POP3Client}.
 * 
 * <p>
 * This only supports {@code POP3}. Attempts to use POP3S/IMAP(S) will cause an exception.
 * </p>
 * <p>
 * Because the underlying POP3 client needs to implement the existing {@link MailReceiver} interface, it will attempt to retrieve
 * every message available int the POP3 mailbox before any filtering is performed. If the POP3 mailbox is large, or has many
 * messages that need to be filtered; then performance may be impaired.
 * </p>
 * 
 * @config pop3-receiver-factory
 * @author lchan
 * 
 */
@XStreamAlias("pop3-receiver-factory")
public class Pop3ReceiverFactory extends ApacheClientConfig implements MailReceiverFactory {

  private static final List<String> SUPPORTED = Collections.unmodifiableList(Arrays.asList("pop3"));

  private Integer connectTimeout;
  private Integer receiveBufferSize;
  private Integer sendBufferSize;
  private Boolean tcpNoDelay;
  private Boolean keepAlive;
  private Integer timeout;

  @Override
  public MailReceiver createClient(URLName url) throws MailException {
    if (!SUPPORTED.contains(url.getProtocol().toLowerCase())) {
      throw new MailException(url.getProtocol() + " is not supported by this factory");
    }
    return new ApachePOP3(url, this);
  }

  @Override
  POP3Client preConnectConfigure(POP3Client client) throws MailException {
    try {
      if (getConnectTimeout() != null) {
        client.setConnectTimeout(getConnectTimeout().intValue());
      }
      if (getReceiveBufferSize() != null) {
        client.setReceiveBufferSize(getReceiveBufferSize().intValue());
      }
      if (getSendBufferSize() != null) {
        client.setSendBufferSize(getSendBufferSize().intValue());
      }
      if (getTimeout() != null) {
        client.setDefaultTimeout(getTimeout().intValue());
      }
    }
    catch (SocketException e) {
      throw new MailException(e);
    }
    return client;
  }

  @Override
  POP3Client postConnectConfigure(POP3Client client) throws MailException {
    try {
      if (getTcpNoDelay() != null) {
        client.setTcpNoDelay(getTcpNoDelay().booleanValue());
      }
      if (getKeepAlive() != null) {
        client.setKeepAlive(getKeepAlive().booleanValue());
      }

    }
    catch (SocketException e) {
      throw new MailException(e);
    }
    return client;
  }

  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Set the connect timeout in ms.
   * 
   * @param i the connect timeout.
   * @see POP3Client#setConnectTimeout(int)
   */
  public void setConnectTimeout(Integer i) {
    this.connectTimeout = i;
  }

  public Integer getReceiveBufferSize() {
    return receiveBufferSize;
  }

  /**
   * Set the receive buffer size in bytes.
   * 
   * @param i the receive buffer size
   * @see POP3Client#setReceiveBufferSize(int)
   */
  public void setReceiveBufferSize(Integer i) {
    this.receiveBufferSize = i;
  }

  public Integer getSendBufferSize() {
    return sendBufferSize;
  }

  /**
   * Set the send buffer size in bytes.
   * 
   * @param i the send buffer size
   * @see POP3Client#setSendBufferSize(int)
   */
  public void setSendBufferSize(Integer i) {
    this.sendBufferSize = i;
  }

  public Boolean getTcpNoDelay() {
    return tcpNoDelay;
  }

  /**
   * Enable / disable Nagle's algorithm
   * 
   * @see POP3Client#setTcpNoDelay(boolean)
   */
  public void setTcpNoDelay(Boolean tcpNoDelay) {
    this.tcpNoDelay = tcpNoDelay;
  }

  public Boolean getKeepAlive() {
    return keepAlive;
  }

  /**
   * Enable/disable keep alive (SO_KEEPALIVE)
   * 
   * @see POP3Client#setKeepAlive(boolean)
   */
  public void setKeepAlive(Boolean keepAlive) {
    this.keepAlive = keepAlive;
  }

  public Integer getTimeout() {
    return timeout;
  }

  /**
   * Set the default timeout for socket operations.
   * 
   * @see POP3Client#setDefaultTimeout(int)
   * @param i the timeout.
   */
  public void setTimeout(Integer i) {
    this.timeout = i;
  }

}
