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

package com.adaptris.core.http.server;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link HttpStatusProvider} that can derive the status and text from {@link AdaptrisMessage} metadata.
 *
 * <p>If the configured metadata key does not exist / non-numeric, then {@link #getDefaultStatus()} is
 * used to provide the Status.
 * </p>
 * 
 * @config http-metadata-status
 * 
 * @author lchan
 */
@XStreamAlias("http-metadata-status")
public class MetadataStatusProvider implements HttpStatusProvider {

  @NotBlank
  private String codeKey;
  @AdvancedConfig
  private String textKey;
  @NotNull
  private HttpStatus defaultStatus;

  public MetadataStatusProvider() {
    setDefaultStatus(HttpStatus.INTERNAL_ERROR_500);
  }

  public MetadataStatusProvider(String codeKey) {
    this(codeKey, null);
  }


  public MetadataStatusProvider(String codeKey, String textKey) {
    this();
    setCodeKey(codeKey);
    setTextKey(textKey);
  }


  @Override
  public Status getStatus(AdaptrisMessage msg) {
    int code = toInt(msg.getMetadataValue(getCodeKey()));
    return new HttpStatusBuilder().withCode(code).withText(msg.getMetadataValue(getTextKey())).build();
  }


  private int toInt(String val) {
    int code = getDefaultStatus().getStatusCode();
    if (!isBlank(val) && isNumeric(val)) {
      code = Integer.parseInt(val);
    }
    return code;
  }


  public String getCodeKey() {
    return codeKey;
  }


  /**
   * Set the metadata where the status code will be taken.
   * 
   * @param key the metadata key.
   */
  public void setCodeKey(String key) {
    this.codeKey = Args.notBlank(key, "status code key");
  }


  public String getTextKey() {
    return textKey;
  }


  /**
   * Set the metadata where the status text will be taken.
   * 
   * <p>Note that for {@link com.adaptris.core.http.jetty.ResponseProducer} any values associated with the key will be ignored as
   * that will use {@link javax.servlet.http.HttpServletResponse#setStatus(int)} method only. This is only included for
   * completeness.
   * </p>
   * @param k the optional key for the status text
   */
  public void setTextKey(String k) {
    this.textKey = k;
  }


  public HttpStatus getDefaultStatus() {
    return defaultStatus;
  }


  /**
   * Set the default status in the event that it cannot be derived from metadata.
   * 
   * @param status the default status, which defaults to {@link HttpStatusProvider.HttpStatus#INTERNAL_ERROR_500}.
   */
  public void setDefaultStatus(HttpStatus status) {
    this.defaultStatus = status;
  }

}
