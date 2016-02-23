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

package com.adaptris.mail;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.URLName;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3SClient;

import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MailReceiverFactory} that supports POP3S using the commons net {@link POP3SClient}.
 * 
 * <p>
 * This only supports {@code POP3S}. Attempts to use POP3/IMAP(S) will cause an exception. While the underlying protocol is still
 * POP3; This factory will <strong>always</strong> attempt to initialise TLS in one of two ways:
 * </p>
 * <ul>
 * <li>Implicit TLS means that the negotiation happens immediately after the initial connect</li>
 * <li>Explicit TLS means that a {@code STLS} command is sent immediately after the initial connect.</li>
 * </ul>
 * <p>
 * Because the underlying POP3 client needs to implement the existing {@link MailReceiver} interface, it will attempt to retrieve
 * every message available int the POP3 mailbox before any filtering is performed. If the POP3 mailbox is large, or has many
 * messages that need to be filtered; then performance may be impaired.
 * </p>
 * 
 * @config pop3s-receiver-factory
 * @author lchan
 * 
 */
@XStreamAlias("pop3s-receiver-factory")
@DisplayOrder(order = {"implicitTls", "alwaysTrust", "protocols", "cipherSuites", "connectTimeout", "timeout", "receiveBufferSize",
    "sendBufferSize", "tcpNoDelay", "keepAlive"})
public class Pop3sReceiverFactory extends Pop3ReceiverFactory {

  private static final List<String> SUPPORTED = Collections.unmodifiableList(Arrays.asList("pop3s"));
  private Boolean implicitTls;
  private Boolean alwaysTrust;
  private String cipherSuites;
  private String protocols;

  @Override
  public MailReceiver createClient(URLName url) throws MailException {
    if (!SUPPORTED.contains(url.getProtocol().toLowerCase())) {
      throw new MailException(url.getProtocol() + " is not supported by this factory");
    }
    return new ApachePOP3S(url, this, implicitTLS());
  }

  @Override
  POP3SClient preConnectConfigure(POP3Client client) throws MailException {
    POP3SClient pop3s = (POP3SClient) super.preConnectConfigure(client);
    if (alwaysTrust()) {
      pop3s.setTrustManager(new AlwaysTrustManager());
    }
    if (getCipherSuites() != null) {
      pop3s.setEnabledCipherSuites(asArray(getCipherSuites()));
    }
    if (getProtocols() != null) {
      pop3s.setEnabledProtocols(asArray(getProtocols()));
    }
    return pop3s;
  }

  @Override
  POP3SClient postConnectConfigure(POP3Client client) throws MailException {
    POP3SClient pop3s = (POP3SClient) super.postConnectConfigure(client);
    return pop3s;
  }

  public Boolean getImplicitTls() {
    return implicitTls;
  }

  /**
   * Turn Implicit TLS mode on or off.
   * <p>
   * ImplicitTLS means that TLS negotiation is peformed automatically after connect to the target server. If false, then the
   * {@code STLS} command must be manually executed before trying to login. This is equivalent to the setting the standard javamail
   * properties {@code mail.pop3.starttls.enable=true} and {@code mail.pop3.starttls.required=true}.
   * </p>
   * 
   * @param b true to enable implicit TLS; default is null (true).
   */
  public void setImplicitTls(Boolean b) {
    this.implicitTls = b;
  }

  boolean implicitTLS() {
    return getImplicitTls() != null ? getImplicitTls().booleanValue() : true;
  }

  public Boolean getAlwaysTrust() {
    return alwaysTrust;
  }

  /**
   * Always trust the server.
   * <p>
   * The server may not always present a valid certificate; in which case you might end up getting lots of exceptions saying
   * {@code unable to find valid certification path to requested target}. Setting this to be true will cause no validation of the
   * certificate chain. Setting this to be true may have a knock on effect on other components that use a {@link SSLContext}.
   * </p>
   * 
   * @param b always trust the server, default is null (false).
   */
  public void setAlwaysTrust(Boolean b) {
    this.alwaysTrust = b;
  }

  boolean alwaysTrust() {
    return getAlwaysTrust() != null ? getAlwaysTrust().booleanValue() : false;
  }

  public String getCipherSuites() {
    return cipherSuites;
  }

  /**
   * Set the cipher suites to be supported for TLS.
   * 
   * @param s a comma separated list of cipher suites
   * @see POP3SClient#setEnabledCipherSuites(String[])
   */
  public void setCipherSuites(String s) {
    this.cipherSuites = s;
  }

  public String getProtocols() {
    return protocols;
  }

  /**
   * Set the protocol versions supported for TLS.
   * 
   * @param s a comma separated list of protocol versions e.g. {@code SSLv2, SSLv3, TLSv1, TLSv1.1, SSLv2Hello}
   * @see POP3SClient#setEnabledProtocols(String[])
   */
  public void setProtocols(String s) {
    this.protocols = s;
  }

  private static String[] asArray(String s) {
    StringTokenizer st = new StringTokenizer(s, ",");
    List<String> l = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      String tok = st.nextToken().trim();
      if (!isEmpty(tok)) l.add(tok);
    }
    return l.toArray(new String[0]);
  }

  private class AlwaysTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

}
