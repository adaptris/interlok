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
package com.adaptris.core.http.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.Removal;
import com.adaptris.core.http.HttpConstants;

/**
 * Build an {@link HttpConstants#AUTHORIZATION} header from metadata.
 * 
 * @author gdries
 * @deprecated since 3.6.0 use {@link com.adaptris.core.http.client.net.MetadataAuthorizationHeader} instead.
 */
@Deprecated
@Removal(version = "3.9.0", message = "Use com.adaptris.core.http.client.net.MetadataAuthorizationHeader instead")
public class MetadataAuthorizationHeader extends com.adaptris.core.http.client.net.MetadataAuthorizationHeader {

  private static transient boolean warningLogged;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public MetadataAuthorizationHeader() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          com.adaptris.core.http.client.net.ConfiguredAuthorizationHeader.class.getName());
      warningLogged = true;
    }
  }
}
