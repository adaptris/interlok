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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.conditional.conditions.CaseDefault;
import com.adaptris.core.services.conditional.conditions.ConditionExpression;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;

public class SwitchTest extends ConditionalServiceExample {


  @Override
  protected Switch retrieveObjectForSampleConfig() {
    return createForTests();
  }

  @Test
  public void testService() throws Exception {
    Switch service = createForTests();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("myKey", "2");
    execute(service, msg);
    assertTrue(msg.headersContainsKey("case"));
    assertEquals("=2", msg.getMetadataValue("case"));
  }

  @Test
  public void testService_NoMatch() throws Exception {
    Switch service = createForTests();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("myKey", "17");
    execute(service, msg);
    assertFalse(msg.headersContainsKey("case"));
  }

  @Test
  public void testService_Failure() throws Exception {
    Switch service = createForTests();
    service.getCases().add(new Case().withCondition(new CaseDefault())
        .withService(new ThrowExceptionService(new ConfiguredException("always-fail"))));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("myKey", "17");
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  private Switch createForTests() {
    Switch service = new Switch().withCases(
        new Case().withCondition(new ConditionExpression().withAlgorithm("%message{myKey} == 1"))
            .withService(
                new ServiceList(new AddMetadataService(new MetadataElement("case", "=1")))),
        new Case().withCondition(new ConditionExpression().withAlgorithm("%message{myKey} == 2"))
            .withService(
                new ServiceList(new AddMetadataService(new MetadataElement("case", "=2")))),
        new Case().withCondition(new ConditionExpression().withAlgorithm("%message{myKey} == 3"))
            .withService(new ServiceList(new AddMetadataService(new MetadataElement("case", "=3"))))

    );
    return service;
  }

}
