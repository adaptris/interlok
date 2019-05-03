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

package com.adaptris.core.services.conditional;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;

/**
 * <p>
 * Conditions are used with logical expressions in configuration such as {@link IfElse} and {@link While}.
 * </p>
 * <p>
 * Logical expressions allow us to branch through services based on the results of Conditions.  Conditions generally test for equality, nulls and not-nulls against a message payload or metadata item.
 * </p>
 * @author amcgrath
 *
 */
public interface Condition extends ComponentLifecycle {
  
  public boolean evaluate(AdaptrisMessage message) throws CoreException;

}
