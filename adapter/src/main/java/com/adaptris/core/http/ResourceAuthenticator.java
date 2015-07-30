package com.adaptris.core.http;

import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Interface used to delegate authentication for network resources.
 * 
 * @author lchan
 * 
 */
public interface ResourceAuthenticator {

  PasswordAuthentication authenticate(ResourceTarget target);

  /**
   * Simply allows access to the protected {@link java.net.Authenticator} methods.
   * 
   * 
   */
  public final class ResourceTarget {
    private String requestingHost;
    private InetAddress requestingSite;
    private int requestingPort;
    private String requestingProtocol;
    private String requestingPrompt;
    private String requestingScheme;
    private URL requestingURL;
    private RequestorType requestorType;

    public final String getRequestingHost() {
      return requestingHost;
    }

    public final void setRequestingHost(String requestingHost) {
      this.requestingHost = requestingHost;
    }

    public final InetAddress getRequestingSite() {
      return requestingSite;
    }

    public final void setRequestingSite(InetAddress requestingSite) {
      this.requestingSite = requestingSite;
    }

    public final int getRequestingPort() {
      return requestingPort;
    }

    public final void setRequestingPort(int requestingPort) {
      this.requestingPort = requestingPort;
    }

    public final String getRequestingProtocol() {
      return requestingProtocol;
    }

    public final void setRequestingProtocol(String requestingProtocol) {
      this.requestingProtocol = requestingProtocol;
    }

    public final String getRequestingPrompt() {
      return requestingPrompt;
    }

    public final void setRequestingPrompt(String requestingPrompt) {
      this.requestingPrompt = requestingPrompt;
    }

    public final String getRequestingScheme() {
      return requestingScheme;
    }

    public final void setRequestingScheme(String requestingScheme) {
      this.requestingScheme = requestingScheme;
    }

    public final URL getRequestingURL() {
      return requestingURL;
    }

    public final void setRequestingURL(URL requestingURL) {
      this.requestingURL = requestingURL;
    }

    public final RequestorType getRequestorType() {
      return requestorType;
    }

    public final void setRequestorType(RequestorType requestingAuthType) {
      this.requestorType = requestingAuthType;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("host", getRequestingHost())
          .append("port", getRequestingPort()).append("prompt", getRequestingPrompt()).append("protocol", getRequestingProtocol())
          .append("scheme", getRequestingScheme()).append("site", getRequestingSite()).append("url", getRequestingURL())
          .append("type", getRequestorType()).toString();
    }

  }
}
