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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.jetty.JettyRouteSpec.RouteMatch;

public class JettyRouteSpecTest {

  private static final String NEXT_SERVICE_ID = "nextServiceId";
  private static final String RECORD_ID_VALUE = "123";
  private static final String RECORD_ID = "recordID";
  private static final String URI = "/record/123";
  private static final String REGEX_WITH_GROUP = "^/record/(.*)$";
  private static final String ALT_REGEX_WITH_GROUP = "^/record/123$";

  @Test
  public void testBuild_Match_ChangedPattern() throws Exception {
    JettyRouteSpec spec =
        new JettyRouteSpec(ALT_REGEX_WITH_GROUP, "POST", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID);
    assertNotNull(spec.build("GET", URI));
    spec.setUrlPattern(REGEX_WITH_GROUP);
    RouteMatch match = spec.build("POST", URI);
    assertTrue(match.matches());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    match.apply(msg);
    assertTrue(msg.headersContainsKey(RECORD_ID));
    assertEquals(RECORD_ID_VALUE, msg.getMetadataValue(RECORD_ID));
    assertEquals(NEXT_SERVICE_ID, msg.getNextServiceId());
  }

  @Test
  public void testBuild_Match_Method_URI() throws Exception {
    JettyRouteSpec spec =
        new JettyRouteSpec(REGEX_WITH_GROUP, "POST", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID);
    assertNotNull(spec.build("GET", URI));
    RouteMatch match = spec.build("POST", URI);
    assertTrue(match.matches());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    match.apply(msg);
    assertTrue(msg.headersContainsKey(RECORD_ID));
    assertEquals(RECORD_ID_VALUE, msg.getMetadataValue(RECORD_ID));
    assertEquals(NEXT_SERVICE_ID, msg.getNextServiceId());
  }

  @Test
  public void testBuild_Match_URI() throws Exception {
    JettyRouteSpec spec =
        new JettyRouteSpec(REGEX_WITH_GROUP, "", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID);
    RouteMatch match = spec.build("POST", URI);
    assertTrue(match.matches());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    match.apply(msg);
    assertTrue(msg.headersContainsKey(RECORD_ID));
    assertEquals(RECORD_ID_VALUE, msg.getMetadataValue(RECORD_ID));
    assertEquals(NEXT_SERVICE_ID, msg.getNextServiceId());
  }


  @Test
  public void testBuild_NoMatch_Method_URI() throws Exception {
    JettyRouteSpec spec =
        new JettyRouteSpec(REGEX_WITH_GROUP, "POST", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID);
    RouteMatch match = spec.build("GET", URI);
    assertFalse(match.matches());
  }

  @Test
  public void testBuild_NoMatch_URI() throws Exception {
    JettyRouteSpec spec =
        new JettyRouteSpec(REGEX_WITH_GROUP, "", Arrays.asList(RECORD_ID), NEXT_SERVICE_ID);
    RouteMatch match = spec.build("GET", "/does/not/match");
    assertFalse(match.matches());
  }


  @Test(expected = ServiceException.class)
  public void testBuild_MetadataMismatch() throws Exception {
    JettyRouteSpec spec =
        new JettyRouteSpec(REGEX_WITH_GROUP, "POST", new ArrayList(), NEXT_SERVICE_ID);
    RouteMatch match = spec.build("POST", URI);
  }
}
