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

package com.adaptris.core.services;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.exception.ExceptionServiceExample;

@SuppressWarnings("deprecation")
public class AlwaysFailServiceTest extends ExceptionServiceExample {

  @Test
  public void testService() throws Exception {
    try {
      execute(new AlwaysFailService(), AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("AlwaysFail didn't fail!");
    } catch (ServiceException e) {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new AlwaysFailService();
  }

}
