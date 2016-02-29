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

package com.adaptris.core.http.jetty;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link com.adaptris.core.AdaptrisMessageProducer} writes to the {@code HttpServletResponse} object metadata
 * provided by the Jetty engine.
 * 
 * @config jetty-standard-reponse-producer
 * @deprecated since 3.2.0; XStreamAlias has a spelling typo, use {@link StandardResponseProducer} instead; this will be removed
 *             without warning.
 * @author lchan
 *
 */
@XStreamAlias("jetty-standard-reponse-producer")
@Deprecated
public class StandardReponseProducer extends StandardResponseProducer {

  private static transient boolean warningLogged;

  public StandardReponseProducer() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(), StandardResponseProducer.class.getName());
      warningLogged = true;
    }
  }

}
