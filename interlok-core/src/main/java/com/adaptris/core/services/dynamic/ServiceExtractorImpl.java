/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
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
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Service;

public abstract class ServiceExtractorImpl implements ServiceExtractor {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public ServiceExtractorImpl() {

  }

  // We intentionally override getInputStream() since it's deprecated, so that
  // if people choose to extend this class, they don't get broken.
  public abstract InputStream getInputStream(AdaptrisMessage msg) throws Exception;

  @Override
  public Service getService(AdaptrisMessage msg, AdaptrisMarshaller m) throws Exception {
    try (InputStream in = getInputStream(msg)) {
      return (Service) m.unmarshal(in);
    }
  }
}
