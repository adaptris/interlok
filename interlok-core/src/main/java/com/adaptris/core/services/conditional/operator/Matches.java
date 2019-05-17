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

package com.adaptris.core.services.conditional.operator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link Operator} simply tests two values for using {@link String#matches(String)}.
 * </p>
 * <p>
 * The first value used in the equality test is the {@link Condition} that this {@link Operator} is
 * configured for; which could be the message payload or a metadata item for example. <br/>
 * The second value is the static value configured for this operator.
 * </p>
 * <p>
 * The static value can be a literal value; "myValue" or can be metadata resolved for example; <br/>
 * 
 * <pre>
 *  <value>%message{myKey}</value>
 * </pre>
 * 
 * The above will test the metadata value identified by the metadata key "myKey".
 * </p>
 * 
 * @config matches
 *
 */
@XStreamAlias("matches")
@AdapterComponent
@ComponentProfile(summary = "Tests that a configured value matches the supplied value.",
    tag = "conditional,operator")
public class Matches implements Operator {

  @InputFieldHint(expression = true)
  private String value;
  
  @Override
  public boolean apply(AdaptrisMessage msg, String obj) {
    return msg.resolve(obj).matches(msg.resolve(getValue()));
  }

  public String getValue() {
    return value;
  }

  /**
   * Set the value to match against.
   * 
   * @param value the value which conforms to a {@link java.util.regex.Pattern}.
   */
  public void setValue(String value) {
    this.value = value;
  }

}
