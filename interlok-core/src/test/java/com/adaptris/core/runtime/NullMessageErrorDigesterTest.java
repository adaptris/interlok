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

package com.adaptris.core.runtime;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Workflow;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class NullMessageErrorDigesterTest extends BaseCase {

  @Test
  public void testLifecycle() throws Exception {
    MessageErrorDigester digester = createDigester();
    LifecycleHelper.init(digester);
    LifecycleHelper.start(digester);
    LifecycleHelper.stop(digester);
    LifecycleHelper.close(digester);
  }

  @Test
  public void testDigest() throws Exception {
    MessageErrorDigester digester = createDigester();
    try {
      start(digester);
      List<AdaptrisMessage> msgs = createMessages(5, 1);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      assertEquals(0, digester.getTotalErrorCount());
    }
    finally {
      stop(digester);
    }
  }

  private MessageErrorDigester createDigester() {
    return new NullMessageErrorDigester();
  }

  private List<AdaptrisMessage> createMessages(int size, int start) {
    List<AdaptrisMessage> errors = new ArrayList<AdaptrisMessage>();
    for (int i = 0; i < size; i++) {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, "workflow" + (i + start));
      errors.add(msg);
    }
    return errors;
  }

}
