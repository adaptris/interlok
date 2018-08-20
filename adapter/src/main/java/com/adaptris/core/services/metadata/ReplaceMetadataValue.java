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

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>Service</code> that adds a performs a simple find and replace on the specified metadata value.
 * </p>
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will taken and if the search-value matches, then
 * the replacement-value will be used as the replacement.
 * </p>
 * <p>
 * You can specify matchgroups as part of the replacment by using the standard {@code $n} syntax. It also supports the special
 * syntax of <code>{n}</code> to indicate match groups, but this is not guaranteed to work in all instances and is only made
 * available for legacy purposes.
 * </p>
 * 
 * @config replace-metadata-value
 * 
 * 
 */
@XStreamAlias("replace-metadata-value")
@AdapterComponent
@ComponentProfile(summary = "Perform a find and replace on metadata", tag = "service,metadata")
@DisplayOrder(order = {"metadataKeyRegexp", "searchValue", "replacementValue", "replaceAll"})
public class ReplaceMetadataValue extends ReformatMetadata {

  @NotBlank
  private String searchValue;
  @InputFieldHint(expression = true)
  private String replacementValue;
  @InputFieldDefault(value = "false")
  private Boolean replaceAll;
  private static final String MATCH_GROUP_REGEX = "(.*?)\\{([0-9]+?)\\}(.*)";
  private transient Pattern matchGroupPattern;
  private transient Pattern searchPattern;

  public ReplaceMetadataValue() {
    super();
    setReplaceAll(false);
  }

  public ReplaceMetadataValue(String regexp, String searchFor, boolean all, String replacement) {
    super(regexp);
    setReplaceAll(all);
    setSearchValue(searchFor);
    setReplacementValue(replacement);
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getSearchValue(), "searchValue");
      matchGroupPattern = Pattern.compile(MATCH_GROUP_REGEX);
      searchPattern = Pattern.compile(getSearchValue());
      super.initService();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public String reformat(String src, AdaptrisMessage msg) throws Exception {
    Matcher searchMatcher = searchPattern.matcher(src);
    String replacement = buildReplacementValue(searchMatcher, msg.resolve(replacementValue()));
    return replaceAll() ? searchMatcher.replaceAll(replacement) : searchMatcher.replaceFirst(replacement);
  }

  private String buildReplacementValue(Matcher metadataValue, String replacement) {
    String result = replacement;
    Matcher matchGroup = matchGroupPattern.matcher(replacement);
    if (matchGroup.matches() && metadataValue.matches()) {
      int group = Integer.valueOf(matchGroup.group(2)).intValue();
      result = matchGroup.group(1) + metadataValue.group(group) + matchGroup.group(3);
      if (matchGroupPattern.matcher(result).matches()) {
        return buildReplacementValue(metadataValue, result);
      }
    }
    return result;
  }

  public String getSearchValue() {
    return searchValue;
  }

  /**
   * The value to search for within the metadata value.
   *
   * @param s the regular expression to search for.
   */
  public void setSearchValue(String s) {
    this.searchValue = s;
  }

  public String getReplacementValue() {
    return replacementValue;
  }

  /**
   * The replacement value.
   * <p>
   * The standard java regexp behaviour of using {@code $n} to indicate the corresponding match group is supported.
   * </p>
   *
   * @param s the replacement value.
   */
  public void setReplacementValue(String s) {
    this.replacementValue = s;
  }

  protected String replacementValue() {
    return defaultIfEmpty(getReplacementValue(), "");
  }

  public Boolean getReplaceAll() {
    return replaceAll;
  }

  /**
   * Specify whether or not to replace all occurences of
   * {@link #setSearchValue(String)}
   *
   * @param s true to replace all occurences.
   */
  public void setReplaceAll(Boolean s) {
    this.replaceAll = s;
  }

  boolean replaceAll() {
    return getReplaceAll() != null ? getReplaceAll().booleanValue() : false;
  }
}
