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

package com.adaptris.core.http.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.HttpServiceExample;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.DateFormatUtil;

public class GetOauthTokenTest extends HttpServiceExample {
  private static final String TEXT = "ABCDEFG";


  @Test
  public void testService_Lifecycle() throws Exception {
    GetOauthToken service = new GetOauthToken();
    try {
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (Exception expected) {

    }
    service.setAccessTokenBuilder(new DummyAccessTokenBuilder());
    LifecycleHelper.stopAndClose(LifecycleHelper.initAndStart(service));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testService() throws Exception {
    long now = System.currentTimeMillis();
    String expiryDate = DateFormatUtil.format(new Date(now));

    AccessToken t = new AccessToken(getName(), now);
    GetOauthToken service =
        new GetOauthToken().withAccessTokenWriter(new MetadataAccessTokenWriter().withTokenKey("Authorization"));
    service.setAccessTokenBuilder(new DummyAccessTokenBuilder(t));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      execute(service, msg);
      assertTrue(msg.headersContainsKey("Authorization"));
      assertEquals("Bearer " + getName(), msg.getMetadataValue("Authorization"));
    }
    finally {

    }
    assertTrue(msg.headersContainsKey("Authorization"));
    assertEquals("Bearer " + getName(), msg.getMetadataValue("Authorization"));
    assertFalse(msg.headersContainsKey("expiry"));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testService_WithError() throws Exception {
    long now = System.currentTimeMillis();
    String expiryDate = DateFormatUtil.format(new Date(now));

    AccessToken t = new AccessToken(getName(), now);
    GetOauthToken service = new GetOauthToken()
        .withAccessTokenWriter(new MetadataAccessTokenWriter().withTokenKey("Authorization"));
    service.setAccessTokenBuilder(new DummyAccessTokenBuilder(t, true));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException e) {

    }
  }

  @Override
  protected GetOauthToken retrieveObjectForSampleConfig() {
    GetOauthToken service = new GetOauthToken().withAccessTokenWriter(new MetadataAccessTokenWriter())
        .withAccessTokenBuilder(new DummyAccessTokenBuilder());
    return service;
  }

}
