/*******************************************************************************
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
 *******************************************************************************/
package com.adaptris.core.http.jetty;

import static com.adaptris.core.http.jetty.JettyRouteConditionTest.RECORD_ID;
import static com.adaptris.core.http.jetty.JettyRouteConditionTest.REGEX_WITH_GROUP;
import static com.adaptris.core.http.jetty.JettyRouteConditionTest.URI;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.jetty.JettyRouteCondition.JettyRoute;
import com.adaptris.core.util.LifecycleHelper;

@SuppressWarnings("deprecation")
public class JettyRouteSpecTest {
  private static final String NEXT_SERVICE_ID = "nextServiceId";

  @Test
  public void testMatch_WithCondition() throws Exception {
    JettyRouteSpec spec =
        LifecycleHelper.initAndStart(new JettyRouteSpec().withCondition(new JettyRouteCondition().withMetadataKeys(RECORD_ID)
        .withMethod("POST").withUrlPattern(REGEX_WITH_GROUP)
        ).withServiceId(NEXT_SERVICE_ID));
    assertNotNull(spec.build("GET", URI));
    JettyRoute match = spec.build("POST", URI);
    assertTrue(match.matches());
    assertTrue(match.metadata().contains(new MetadataElement(RECORD_ID, "")));
  }

  @Test
  public void testtToString() throws Exception {
    assertNotNull(new JettyRouteSpec()
        .withCondition(new JettyRouteCondition().withMetadataKeys(RECORD_ID).withMethod("POST").withUrlPattern(REGEX_WITH_GROUP))
        .withServiceId(NEXT_SERVICE_ID).toString());
  }
}
