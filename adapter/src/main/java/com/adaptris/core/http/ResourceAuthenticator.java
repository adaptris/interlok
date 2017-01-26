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

    public final void setRequestingHost(String host) {
      this.requestingHost = host;
    }

    public ResourceTarget withRequestingHost(String host) {
      setRequestingHost(host);
      return this;
    }

    public final InetAddress getRequestingSite() {
      return requestingSite;
    }

    public final void setRequestingSite(InetAddress site) {
      this.requestingSite = site;
    }

    public ResourceTarget withRequestingSite(InetAddress site) {
      setRequestingSite(site);
      return this;
    }

    public final int getRequestingPort() {
      return requestingPort;
    }

    public final void setRequestingPort(int port) {
      this.requestingPort = port;
    }

    public ResourceTarget withRequestingPort(int port) {
      setRequestingPort(port);
      return this;
    }

    public final String getRequestingProtocol() {
      return requestingProtocol;
    }

    public final void setRequestingProtocol(String protocol) {
      this.requestingProtocol = protocol;
    }

    public ResourceTarget withRequestingProtocol(String protocol) {
      setRequestingProtocol(protocol);
      return this;
    }

    public final String getRequestingPrompt() {
      return requestingPrompt;
    }

    public final void setRequestingPrompt(String prompt) {
      this.requestingPrompt = prompt;
    }

    public ResourceTarget withRequestingPrompt(String prompt) {
      setRequestingPrompt(prompt);
      return this;
    }

    public final String getRequestingScheme() {
      return requestingScheme;
    }

    public final void setRequestingScheme(String scheme) {
      this.requestingScheme = scheme;
    }

    public ResourceTarget withRequestingScheme(String scheme) {
      setRequestingPrompt(scheme);
      return this;
    }

    public final URL getRequestingURL() {
      return requestingURL;
    }

    public final void setRequestingURL(URL url) {
      this.requestingURL = url;
    }

    public ResourceTarget withRequestingURL(URL url) {
      setRequestingURL(url);
      return this;
    }

    public final RequestorType getRequestorType() {
      return requestorType;
    }

    public final void setRequestorType(RequestorType type) {
      this.requestorType = type;
    }

    public ResourceTarget withRequestorType(RequestorType type) {
      setRequestorType(type);
      return this;
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
