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

package com.adaptris.util;

import java.net.InetAddress;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Variation of {@linkplain PlainIdGenerator} which uses the hostname instead of a random sequence of bytes.
 * <p>
 * The ID is generated from a combination of the current hostname, hashcode of the object, and current timestamp.
 * </p>
 * 
 * @config simple-id-generator
 */
@XStreamAlias("simple-id-generator")
public class SimpleIdGenerator extends PlainIdGenerator {

  private transient String host = "";

  private static String localhostname;

  public SimpleIdGenerator() {
    super();
    host = localHostname();
  }

  public SimpleIdGenerator(String separator) {
    this();
    setSeparator(separator);
  }

  @Override
  public String create(Object msg) {
    return host + getSeparator() + (msg == null ? "0" : Integer.toHexString(msg.hashCode())) + getSeparator()
        + System.currentTimeMillis();
  }

  private synchronized static String localHostname() {
    try {
      if (localhostname == null) {
        localhostname = InetAddress.getLocalHost().getHostName();
      }
    }
    catch (Exception e) {
      localhostname = "localhost";
    }
    return localhostname;
  }
}
