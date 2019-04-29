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

package com.adaptris.core.services.dynamic;

import java.io.IOException;
import java.io.InputStream;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default {@link ServiceExtractor} implementation that treats the entire payload as the service.
 * 
 * @config dynamic-default-service-extractor
 * 
 */
@XStreamAlias("dynamic-default-service-extractor")
@ComponentProfile(summary = "Treat the message body as the service to execute.")
// intentionally doesn't extend ServiceExtratorImpl because _coverage_
public class DefaultServiceExtractor implements ServiceExtractor {

  @Override
  public InputStream getInputStream(AdaptrisMessage m) throws ServiceException, IOException {
    return m.getInputStream();
  }

}
