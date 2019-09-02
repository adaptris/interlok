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

package com.adaptris.transform.validate;

import java.util.regex.Pattern;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Match the content against a regular expression.
 * 
 * @config xml-content-regexp
 * 
 */
@XStreamAlias("xml-content-regexp")
@DisplayOrder(order = {"pattern"})
public class RegexpContentValidation implements ContentValidation {

  @NotBlank
  @AutoPopulated
  private String pattern;
  private transient Pattern regexpPattern = null;

  public RegexpContentValidation() {
    setPattern(".*");
  }

  public RegexpContentValidation(String pattern) {
    this();
    setPattern(pattern);
  }

  /**
   *  @see ContentValidation#isValid(java.lang.String)
   */
  @Override
  public boolean isValid(String content) {
    if (regexpPattern == null) {
      regexpPattern = Pattern.compile(getPattern());
    }
    return regexpPattern.matcher(content).matches();
  }

  /**
   *  @see ContentValidation#getMessage()
   */
  @Override
  public String getMessage() {
    return "Element contents did not validate against the pattern " + pattern;
  }

  /**
   * Get the pattern we are matching against.
   * 
   */
  public final String getPattern() {
    return pattern;
  }

  /**
   * Set the pattern we are matching against..
   * 
   * @param pattern the pattern; default is '.*';
   */
  public final void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
