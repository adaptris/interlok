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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.jetty.JettyRouteCondition.JettyRoute;
import com.adaptris.core.util.LifecycleHelper;

@SuppressWarnings("deprecation")
public class JettyRouteSpecTest {
  private static final String NEXT_SERVICE_ID = "nextServiceId";

  @Test
  public void testBuild_Match_ChangedPattern() throws Exception {
    JettyRouteSpec spec =
        LifecycleHelper.initAndStart(new JettyRouteSpec(REGEX_WITH_GROUP, "POST", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID));
    assertNotNull(spec.build("POST", URI));
    // Changing the URL pattern has no effect; since lifecycle.
    spec.setUrlPattern("/hello/world");
    JettyRoute match = spec.build("POST", "/hello/world");
    assertFalse(match.matches());
  }

  @Test
  public void testBuild_Match_Method_URI() throws Exception {
    JettyRouteSpec spec =
        LifecycleHelper.initAndStart(new JettyRouteSpec(REGEX_WITH_GROUP, "POST", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID));
    assertNotNull(spec.build("GET", URI));
    JettyRoute match = spec.build("POST", URI);
    assertTrue(match.matches());
    assertTrue(match.metadata().contains(new MetadataElement(RECORD_ID, "")));
  }

  @Test
  public void testBuild_Match_URI() throws Exception {
    JettyRouteSpec spec =
        LifecycleHelper.initAndStart(new JettyRouteSpec(REGEX_WITH_GROUP, "", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID));
    JettyRoute match = spec.build("POST", URI);
    assertTrue(match.matches());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertTrue(match.metadata().contains(new MetadataElement(RECORD_ID, "")));
  }


  @Test
  public void testBuild_NoMatch_Method_URI() throws Exception {
    JettyRouteSpec spec =
        LifecycleHelper.initAndStart(new JettyRouteSpec(REGEX_WITH_GROUP, "POST", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID));
    JettyRoute match = spec.build("GET", URI);
    assertFalse(match.matches());
  }

  @Test
  public void testBuild_NoMatch_URI() throws Exception {
    JettyRouteSpec spec =
        LifecycleHelper.initAndStart(new JettyRouteSpec(REGEX_WITH_GROUP, "", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID));
    JettyRoute match = spec.build("GET", "/does/not/match");
    assertFalse(match.matches());
  }


  @Test(expected = CoreException.class)
  public void testBuild_MetadataMismatch() throws Exception {
    JettyRouteSpec spec =
        LifecycleHelper.initAndStart(new JettyRouteSpec(REGEX_WITH_GROUP, "POST", new ArrayList(), NEXT_SERVICE_ID));
    JettyRoute match = spec.build("POST", URI);
  }

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
