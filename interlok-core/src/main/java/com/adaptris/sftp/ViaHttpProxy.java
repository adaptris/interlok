/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.sftp;

import com.adaptris.annotation.DisplayOrder;
import com.jcraft.jsch.ProxyHTTP;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Connect via a HTTP proxy
 * 
 * @author lchan
 *
 */
@JacksonXmlRootElement(localName = "sftp-via-http-proxy")
@XStreamAlias("sftp-via-http-proxy")
@DisplayOrder(order = {"proxy", "username", "password"})
public class ViaHttpProxy extends ViaProxy {

  public ViaHttpProxy() {
    super();
  }

  public ViaHttpProxy(String proxy) {
    this();
    setProxy(proxy);
  }

  @Override
  protected ProxyHTTP createProxy(String host) {
    return new ProxyHTTP(host);
  }

}
