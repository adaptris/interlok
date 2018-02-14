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

package com.adaptris.transform;

//import com.adaptris.util.Log;
//import com.adaptris.util.LogCategory;
//import com.adaptris.mom.adapter.IhubMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.Args;

/**
 * <p>
 * This is the central class in the package and it represents the base class of
 * all transform frameworks. The {@link #transform(Source,Source,Target)} method
 * of a concrete sub-classed implementation performs the actual transformation
 * of data.
 * </p>
 *
 * <p>
 * The class has methods which allow a client application to add transformation
 * rules ahead of their actual use. If a rule is unknown to an instance of a
 * transform framework at the time of invoking the
 * {@link #transform(Source,Source,Target)} method, then the rule is added to
 * the object's internal list of rules for further reuse.
 * </p>
 *
 * @author Trevor Vaughan
 * @version 0.1 April 2001
 */
public abstract class TransformFramework {

  /**
   * <p>
   * Internal store of transformation rules.
   * </p>
   */
  protected RuleList ruleList = new RuleList();

  /**
   * <p>
   * Used for providing logging information.
   * </p>
   */
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  public TransformFramework() {
  }

  /**
   * <p>
   * Returns the index position of a rule contained in
   * <code>TransformFramework</code>.
   * </p>
   *
   * @return the index position of the rule or -1 if not found.
   * @param rule the rule to find the index of.
   * @see #removeRule(int)
   */
  public int indexOfRule(Source rule) {
    return ruleList.indexOfKey(rule);
  }

  /**
   * <p>
   * Returns the number of rules currently held within
   * <code>TransformFramework</code>.
   * </p>
   *
   * @return the number of rules.
   */
  public int getNumRules() {
    return ruleList.size();
  }

  /**
   * <p>
   * Removes a rule.
   * </p>
   *
   * @param rule the rule to remove.
   * @see #indexOfRule(Source)
   * @see #removeRule(int)
   */
  public void removeRule(Source rule) {
    ruleList.remove(Args.notNull(rule, "rule"));
    log.trace("TransformFramework removeRule() invoked: rule [{}]", rule);
  }

  /**
   * <p>
   * Removes a rule.
   * </p>
   *
   * @param index the index position of the rule to remove.
   * @see #indexOfRule(Source)
   * @see #removeRule(Source)
   */
  public void removeRule(int index) {
    ruleList.remove(index);
    log.trace("TransformFramework removeRule() invoked: index {}", index);
  }

  /**
   * <p>
   * Removes all the rules from <code>TransformFramework</code>.
   * </p>
   *
   * @see #reset()
   */
  public void removeRules() {
    ruleList.removeAll();
    log.trace("TransformFramework removeRules() invoked");
  }

  /**
   * <p>
   * Performs the transformation.
   * </p>
   *
   * <p>
   * This object must not be reused until the method synchronously returns to
   * the caller.
   * </p>
   *
   * @param in the input that is to be transformed.
   * @param rule the specific rule to use in the transformation.
   * @param out the output of the transformation.
   * @throws Exception when there is an error detected during transformation.
   */
  public abstract void transform(Source in, Source rule, Target out)
      throws Exception;

  /**
   * <p>
   * Adds a rule. This method is made abstract as it is up to the sub-class to
   * determine how a rule is to be optimised. Refer to <a href="RuleList.html"
   * >RuleList</a> for further details.
   * </p>
   *
   * @param rule the rule to add.
   * @throws Exception when there is an error detected adding a rule.
   */
  public abstract void addRule(Source rule) throws Exception;

} // class TransformFramework
