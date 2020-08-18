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

package com.adaptris.core.services.exception;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;

public class ClearExceptionServiceTest extends ExceptionServiceExample {

  @Test
  public void testClearException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception());
    execute(new ClearExceptionService(), msg);
    assertFalse(msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
  }

  @Test
  public void testClearException_NoException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(new ClearExceptionService(), msg);
    assertFalse(msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
  }

  @Override
  protected ClearExceptionService retrieveObjectForSampleConfig() {
    return new ClearExceptionService();
  }

}
