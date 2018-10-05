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
 * @deprecated use {@link SimpleHttpProducer} instead (since 2.6.1)
 * <p>
 * In the adapter configuration file this class is aliased as <b>post-method-producer</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@Deprecated
@XStreamAlias("post-method-producer")
public class PostMethodProducer extends SimpleHttpProducer {

  public PostMethodProducer() {
    super();
    log.warn(this.getClass().getCanonicalName() + " is deprecated, use "
        + SimpleHttpProducer.class.getCanonicalName() + " instead");
    setMethod("POST");
  }
}
