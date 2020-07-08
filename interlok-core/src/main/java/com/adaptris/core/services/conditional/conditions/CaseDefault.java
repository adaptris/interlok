/*
 * Copyright 2019 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.services.conditional.conditions;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Switch;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Condition that is always true.
 * 
 * <p>
 * The use case for this is to simulate the 'default' branch of a {@link Switch} service; add this conditional as the last
 * {@link Condition} configuration in your {@link Switch} service to have a default set of services that are executed.
 * </p>
 * 
 * @config case-default
 */
@XStreamAlias("case-default")
@ComponentProfile(summary = "condition that is always true.", tag = "condition", since = "3.9.0")
public class CaseDefault implements Condition {

  @Override
  public boolean evaluate(AdaptrisMessage message) throws CoreException {
    return true;
  }
}
