/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.core.services.conditional.conditions;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.conditional.Condition;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import bsh.Interpreter;

/**
 * <p>
 * This {@link Condition} allows you to specify a boolean evaluated expression, with static values
 * and resolved metadata values.
 * </p>
 * <p>
 * If your expression evaluates to "true", then this condition passes.
 * </p>
 * <p>
 * Static values mixed with metadata values allow you to create boolean expressions, such as; <br/>
 * <table>
 * <tr>
 * <th>Example description</th>
 * <th>Example Expression</th>
 * </tr>
 * <tr>
 * <td>Is the metadata value identified by key "myKey" equal to the value 1</td>
 * <td>%message{myKey} == 1</td>
 * </tr>
 * <tr>
 * <td>Is the metadata value identified by key "myKey" equal to the metadata item "myOtherKey"</td>
 * <td>%message{myKey} == %message{myOtherKey}</td>
 * </tr>
 * <tr>
 * <td>Is the metadata value identified by key "myKey" greater than "myOtherKey" plus 100</td>
 * <td>%message{myKey} &gt; (%message{myOtherKey} + 100)</td>
 * </tr>
 * </table>
 * 
 * </p>
 * 
 * @config expression
 * @author amcgrath
 *
 */
@XStreamAlias("expression")
@AdapterComponent
@ComponentProfile(summary = "Tests a static algorithm for a boolean result.", tag = "condition")
@DisplayOrder(order = {"algorithm"})
public class ConditionExpression extends ConditionImpl {
  
  @InputFieldHint(expression = true)
  private String algorithm;

  public ConditionExpression() {
  }

  @Override
  public boolean evaluate(AdaptrisMessage msg) throws CoreException {
    boolean rc = false;
    try {
      Interpreter interpreter = new Interpreter();
      String expr = msg.resolve(this.getAlgorithm());
      interpreter.eval("result = (" + expr + ")");
      String stringResult = interpreter.get("result").toString();
      logCondition("{}: {} evaluated to : {}", getClass().getSimpleName(), expr, stringResult);
      rc = BooleanUtils.toBoolean(stringResult);
    } catch (Exception ex) {
      throw new ServiceException(ex);
    }
    return rc;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public ConditionExpression withAlgorithm(String s) {
    setAlgorithm(s);
    return this;
  }
}
