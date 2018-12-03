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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Stage within the validation.
 * 
 * @config xml-validation-stage
 * 
 * @author sellidge
 */
@XStreamAlias("xml-validation-stage")
@DisplayOrder(order = {"iterationXpath", "elementXpath", "rules", "failOnIterateFailure"})
public class ValidationStage {

  @NotNull
  @AutoPopulated
  private String iterationXpath = "";
  @NotNull
  @AutoPopulated
  private String elementXpath = "";
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean failOnIterateFailure;

  @NotNull
  @Valid
  @AutoPopulated
  private List<ContentValidation> rules;

  public ValidationStage() {
    setRules(new ArrayList<ContentValidation>());
  }
  
  public ValidationStage(String iterationXpath, String elementXpath, ContentValidation... ruleList) {
    this(iterationXpath, elementXpath, new ArrayList<ContentValidation>(Arrays.asList(ruleList)));
  }

  public ValidationStage(String iterationXpath, String elementXpath, List<ContentValidation> ruleList) {
    this();
    setIterationXpath(iterationXpath);
    setElementXpath(elementXpath);
    setRules(ruleList);
  }

  /** Set the iteration xpath.
   *
   * @param xpath the xpath
   */
  public void setIterationXpath(String xpath) {
    iterationXpath = xpath;
  }

  /** Get the iteration xpath.
   *
   * @return the configured xpath.
   */
  public String getIterationXpath() {
    return iterationXpath;
  }

  /** Set the element xpath.
   *
   * @param xpath the xpath.
   */
  public void setElementXpath(String xpath) {
    elementXpath = xpath;
  }

  /** Get the configured element xpath.
   *
   * @return the xpath.
   */
  public String getElementXpath() {
    return elementXpath;
  }

  /** Add a rule to this stage.
   *
   * @param validationRule the rule.
   */
  public void addRule(ContentValidation validationRule) {
    rules.add(validationRule);
  }

  /** Get the list of configured rules.
   *
   * @return the configured list.
   */
  public List<ContentValidation> getRules() {
    return rules;
  }

  /** Set the list of configured rules.
   *
   * @param l the configured list.
   */
  public void setRules(List<ContentValidation> l) {
    rules = l;
  }

  public Boolean getFailOnIterateFailure() {
    return failOnIterateFailure;
  }

  /**
   * If the {@link #getIterationXpath()} returns a zero length nodelist should we fail.
   * 
   * @param b true to throw an exception of a zero length nodelist is returned, default is null (false)
   */
  public void setFailOnIterateFailure(Boolean b) {
    this.failOnIterateFailure = b;
  }

  public final boolean failOnIteratorFailure() {
    return getFailOnIterateFailure() != null ? getFailOnIterateFailure().booleanValue() : false;
  }

}
