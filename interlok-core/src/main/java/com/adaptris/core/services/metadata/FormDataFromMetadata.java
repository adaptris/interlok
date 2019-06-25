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

package com.adaptris.core.services.metadata;

import java.nio.charset.StandardCharsets;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that creates something suitable to send as {@code application/x-www-url-form-encoded}
 * from metadata.
 * 
 * @config www-url-form-encoded-payload-from-metadata
 * 
 */
@XStreamAlias("www-url-form-encoded-payload-from-metadata")
@AdapterComponent
@ComponentProfile(summary = "Create a application/x-www-url-form-encoded payload from metadata",
    tag = "service,metadata,http,https", since = "3.9.0")
@DisplayOrder(order = {"metadata-filter", "separator"})
public class FormDataFromMetadata extends UrlEncodedMetadataValues {

  public FormDataFromMetadata() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      msg.setContent(buildEncodedString(msg), StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
}
