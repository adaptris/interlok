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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link Condition} targets the message payload. All you need do is choose an {@link Operator}
 * to apply the conditional test.
 * </p>
 * 
 * @config payload
 * @author amcgrath
 *
 */
@XStreamAlias("payload")
@AdapterComponent
@ComponentProfile(summary = "Tests a payload against a configured operator.", tag = "condition")
@DisplayOrder(order = {"operator"})
public class ConditionPayload extends ConditionWithOperator {
  
  @Override
  public boolean evaluate(AdaptrisMessage message) throws CoreException {
    boolean result = operator().apply(message, message.getContent());
    logCondition("{}: evaluating payload {} : result {}", getClass().getSimpleName(), getOperator(), result);
    return result;
  }
}
