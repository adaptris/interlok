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

package com.adaptris.core.http.client;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link RequestMethodProvider} that can derive the method from {@link com.adaptris.core.AdaptrisMessage} metadata.
 *
 * <p>If the configured metadata key does not exist, then {@link #getDefaultMethod()} is
 * used to provide the request method.
 * </p>
 * @config http-metadata-request-method
 * @author lchan
 *
 */
@XStreamAlias("http-metadata-request-method")
public class MetadataRequestMethodProvider implements RequestMethodProvider {

  @NotBlank
  private String metadataKey;
  @AutoPopulated
  @NotNull
  private RequestMethod defaultMethod;


  public MetadataRequestMethodProvider() {
    setDefaultMethod(RequestMethod.POST);
  }

  public MetadataRequestMethodProvider(String key) {
    this(key, RequestMethod.POST);
  }

  public MetadataRequestMethodProvider(String key, RequestMethod defMethod) {
    this();
    setMetadataKey(key);
    setDefaultMethod(defMethod);
  }

  @Override
  public RequestMethod getMethod(AdaptrisMessage msg) {
    if (msg.headersContainsKey(getMetadataKey())) {
      return RequestMethod.valueOf(msg.getMetadataValue(getMetadataKey()).toUpperCase());
    }
    return getDefaultMethod();
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = Args.notBlank(metadataKey, "Metadata Key");
  }

  public RequestMethod getDefaultMethod() {
    return defaultMethod;
  }

  /**
   * Set the default method.
   * 
   * @param m the default method ({@link RequestMethodProvider.RequestMethod#POST}).
   */
  public void setDefaultMethod(RequestMethod m) {
    this.defaultMethod = Args.notNull(m, "Default HTTP Method");
  }

}
