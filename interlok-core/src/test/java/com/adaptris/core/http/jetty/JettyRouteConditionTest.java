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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.util.LifecycleHelper;

public class JettyRouteConditionTest {

  public static final String RECORD_ID_VALUE = "123";
  public static final String RECORD_ID = "recordID";
  public static final String URI = "/record/123";
  public static final String REGEX_WITH_GROUP = "^/record/(.*)$";
  public static final String ALT_REGEX_WITHOUT_GROUP = "^/record/123$";

  @Test
  public void testEvaluate_Match_URI_POST() throws Exception {
    JettyRouteCondition condition = LifecycleHelper
        .initAndStart(new JettyRouteCondition().withMetadataKeys(RECORD_ID).withMethod("POST").withUrlPattern(REGEX_WITH_GROUP));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, URI);
    msg.addMetadata(CoreConstants.HTTP_METHOD, "POST");
    assertTrue(condition.evaluate(msg));
    assertTrue(msg.headersContainsKey(RECORD_ID));
    assertEquals(RECORD_ID_VALUE, msg.getMetadataValue(RECORD_ID));
  }

  @Test
  public void testEvaluate_Match_URI() throws Exception {
    JettyRouteCondition condition = LifecycleHelper
        .initAndStart(new JettyRouteCondition().withMetadataKeys(RECORD_ID).withUrlPattern(REGEX_WITH_GROUP));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, URI);
    msg.addMetadata(CoreConstants.HTTP_METHOD, "POST");
    assertTrue(condition.evaluate(msg));
    assertTrue(msg.headersContainsKey(RECORD_ID));
    assertEquals(RECORD_ID_VALUE, msg.getMetadataValue(RECORD_ID));
  }

  @Test
  public void testEvaluate_NoMatch_METHOD() throws Exception {
    JettyRouteCondition condition =
        LifecycleHelper.initAndStart(
            new JettyRouteCondition().withMetadataKeys(RECORD_ID).withMethod("GET").withUrlPattern(REGEX_WITH_GROUP));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, URI);
    msg.addMetadata(CoreConstants.HTTP_METHOD, "POST");
    assertFalse(condition.evaluate(msg));
  }

  @Test
  public void testEvaluate_NoMatch_URI() throws Exception {
    JettyRouteCondition condition = LifecycleHelper
        .initAndStart(new JettyRouteCondition().withMetadataKeys(RECORD_ID).withMethod("POST").withUrlPattern(REGEX_WITH_GROUP));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, "/hello/world");
    msg.addMetadata(CoreConstants.HTTP_METHOD, "POST");
    assertFalse(condition.evaluate(msg));
  }

  @Test
  public void testEvaluate_NoMetadata() throws Exception {
    JettyRouteCondition condition = LifecycleHelper
        .initAndStart(new JettyRouteCondition().withMethod("POST").withUrlPattern(ALT_REGEX_WITHOUT_GROUP));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, URI);
    msg.addMetadata(CoreConstants.HTTP_METHOD, "POST");
    assertTrue(condition.evaluate(msg));
  }
}
