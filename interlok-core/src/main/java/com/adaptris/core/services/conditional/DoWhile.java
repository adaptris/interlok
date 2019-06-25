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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simulate a do-while loop.
 * 
 * <p>
 * It differs from {@link While} in the same way that a {@code do{ } while(condition);} differs from
 * {@code while(condition) { }}.
 * </p>
 * 
 * @see While
 * @config do-while
 */
@XStreamAlias("do-while")
@AdapterComponent
@ComponentProfile(
    summary = "Runs the configured service/list repeatedly 'WHILE' the configured condition is met.",
    tag = "service,conditional,loop", since = "3.8.4")
@DisplayOrder(order = {"condition", "then", "maxLoops"})
public class DoWhile extends While {
  
  public DoWhile() {
    super();
  }
  
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    int loopCount = 0;
    try {
      do {
        getThen().getService().doService(msg);
        loopCount++;
        if (exceedsMax(loopCount)) {
          log.debug("Reached maximum loops({}), breaking.", maxLoops());
          break;
        }
        log.trace("Testing condition for 'DO-WHILE', with condition class {}",
            this.getCondition().getClass().getSimpleName());
      } while (getCondition().evaluate(msg));
      log.trace("Logical 'DO-WHILE' completed, exiting.");
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
}
