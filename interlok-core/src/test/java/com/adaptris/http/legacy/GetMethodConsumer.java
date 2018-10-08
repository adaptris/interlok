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

package com.adaptris.http.legacy;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>GenericConsumer</code> which handles GETs. <code>handleRequest</code> method is identical to
 * <code>PostMethodConsumer</code>, needs to be refactored.
 * </p>
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 * 
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.jetty.MessageConsumer} instead
 * <p>
 * In the adapter configuration file this class is aliased as <b>http-get-method-consumer</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@Deprecated
@XStreamAlias("http-get-method-consumer")
public class GetMethodConsumer extends GenericConsumer {

  /** @see com.adaptris.http.legacy.GenericConsumer#getMethod() */
  @Override
  protected String getMethod() {
    return "GET";
  }
}
