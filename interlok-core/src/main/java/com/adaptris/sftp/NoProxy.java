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

import com.jcraft.jsch.Proxy;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * No Proxy connection.
 * 
 * @author lchan
 *
 */
@XStreamAlias("sftp-no-proxy")
public class NoProxy implements ProxyBuilder {

  @Override
  public Proxy buildProxy() {
    return null;
  }

}
