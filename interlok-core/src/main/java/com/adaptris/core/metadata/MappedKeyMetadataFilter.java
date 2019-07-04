/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.metadata;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MetadataFilter} implementation that modifies keys matching a specific regular expression to another key name.
 * 
 * <p>
 * Note that this implementation will include all items of metadata. Any keys that match the specified prefix will be replaced using
 * {@link Matcher#replaceAll(String)} by {@link #getReplacement()}.
 * </p>
 * <p>
 * This specific filter is only useful when used as part of a producer that supports filtering which allows you to map header key
 * that are prefixed with a specific text into another header key. This could be useful if you are doing multiple HTTP calls within
 * the same workflow, and you need to keep track of all the individual headers that are returned by each request. So if you have
 * preserved some HTTP headers prefixed with {@code InboundRequest_} then you might want to send specifically those headers back to
 * the requesting client without the prefix. Configure {@code prefix="^InboundRequest_(.*)$"} and {@code replacement="$1"} which
 * means that InboundRequest_User-Agent would become {@code User-Agent} at the point you trigger the producer, but can be treated as
 * {@code InboundRequest_User-Agent} throughout the rest of the workflow.
 * </p>
 * 
 * @config mapped-key-metadata-filter
 *
 */
@XStreamAlias("mapped-key-metadata-filter")
public class MappedKeyMetadataFilter extends MetadataFilterImpl {

  @NotBlank
  private String prefix;
  private String replacement;

  private transient Pattern prefixPattern = null;

  public MappedKeyMetadataFilter() {

  }

  public MappedKeyMetadataFilter(String prefix, String replacement) {
    this();
    setPrefix(prefix);
    setReplacement(replacement);
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    // As we're changing the key, always create a new copy.
    MetadataCollection result = new MetadataCollection();
    for (MetadataElement e : original) {
      result.add(new MetadataElement(renameKey(e.getKey()), e.getValue()));
    }
    return result;
  }

  private String renameKey(String orig) {
    if (prefixPattern == null) {
      prefixPattern = Pattern.compile(prefix);
    }
    Matcher prefixMatcher = prefixPattern.matcher(orig);
    return prefixMatcher.replaceAll(replacementValue());
  }

  public String getPrefix() {
    return prefix;
  }

  /**
   * Set the prefix
   * 
   * @param s the prefix, may be a JDK regular expression (e.g. {@code ^myPrefix(.*)})
   * 
   */
  public void setPrefix(String s) {
    this.prefix = s;
  }

  public String getReplacement() {
    return replacement;
  }

  /**
   * Set the replacement
   * 
   * @param s the replacement value; again may be a match group from your original {@link #setPrefix(String)} - e.g. {@code $1}
   */
  public void setReplacement(String s) {
    this.replacement = s;
  }

  String replacementValue() {
    return !isEmpty(getReplacement()) ? getReplacement() : "";
  }
}
