/*
 * Copyright 2019 Adaptris Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.MetadataHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Extract additional metadata values from an item of metadata.
 * <p>
 * Given a metadata key {@code url} containing {@code /record/zeus/apollo} if you have a regular expression
 * {@code /record/(.*)/(.*)} with metadata keys {@code recordId, childId} then {@code parentId=zeus},
 * {@code childId=apollo} will be set when this service executes.
 * <p>
 * 
 * @config extract-metadata-from-metadata
 */
@XStreamAlias("extract-metadata-from-metadata")
@ComponentProfile(summary = "Extract additional metadata values from an item of metadata", since = "3.9.0",
    tag = "service,metadata")
public class ExtractMetadataService extends MetadataServiceImpl {

  @NotBlank
  private String sourceKey;
  @NotBlank
  private String regularExpression;
  @NotNull
  @AutoPopulated
  private List<String> metadataKeys;

  private transient Pattern regexpPattern;

  public ExtractMetadataService() {
    setMetadataKeys(new ArrayList<>());
  }

  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getSourceKey(), "source-key");
    Args.notBlank(getRegularExpression(), "regular-expression");
  }

  @Override
  protected void initService() throws CoreException {
    super.initService();
    regexpPattern = Pattern.compile(getRegularExpression());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      String value = msg.getMetadataValue(getSourceKey());
      Matcher matcher = regexpPattern.matcher(value);
      if (matcher.matches()) {
        Set<MetadataElement> extracted = MetadataHelper.metadataFromMatchGroups(matcher, metadataKeys());
        logMetadata("Adding metadata: {}", extracted);
        msg.setMetadata(extracted);
      } else {
        log.trace("{} did not match, no metadata set", getRegularExpression());
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public void setSourceKey(String sourceKey) {
    this.sourceKey = sourceKey;
  }

  public ExtractMetadataService withSourceKey(String s) {
    setSourceKey(s);
    return this;
  }


  public String getRegularExpression() {
    return regularExpression;
  }

  public void setRegularExpression(String regExp) {
    this.regularExpression = regExp;
  }

  public ExtractMetadataService withRegularExpression(String s) {
    setRegularExpression(s);
    return this;
  }

  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  public void setMetadataKeys(List<String> metadataKeys) {
    this.metadataKeys = metadataKeys;
  }

  public ExtractMetadataService withMetadataKeys(List<String> keys) {
    setMetadataKeys(keys);
    return this;
  }

  public ExtractMetadataService withMetadataKeys(String... keys) {
    return withMetadataKeys(new ArrayList<>(Arrays.asList(keys)));
  }

  private List<String> metadataKeys() {
    return ObjectUtils.defaultIfNull(getMetadataKeys(), Collections.emptyList());
  }
}
