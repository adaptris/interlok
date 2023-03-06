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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Condition that is never true.
 * 
 * <p>
 * This is included for completeness; it will never fire as a condition, so will be useless at runtime. You could use it to
 * temporarily exclude switch cases from executing.
 * </p>
 * 
 * @config never
 */
@JacksonXmlRootElement(localName = "never")
@XStreamAlias("never")
@ComponentProfile(summary = "condition that is never true.", tag = "condition", since = "3.9.0")
public class ConditionNever implements Condition {

  @Override
  public boolean evaluate(AdaptrisMessage message) throws CoreException {
    return false;
  }
}
