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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.http.HttpServiceExample;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class GetAndCacheOauthTokenTest extends HttpServiceExample {
  private static final String TEXT = "ABCDEFG";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testService_Lifecycle() throws Exception {
    GetAndCacheOauthToken service = new GetAndCacheOauthToken();
    try {
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (Exception expected) {

    }
    service.setAccessTokenBuilder(new DummyAccessTokenBuilder());
    service.setCacheKey("cacheKey");
    LifecycleHelper.stopAndClose(LifecycleHelper.initAndStart(service));
  }

  @Test
  public void testService() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache().withExpiration(new TimeInterval(5L, TimeUnit.SECONDS));
    AccessToken t = new AccessToken(getName());
    GetAndCacheOauthToken service =
        new GetAndCacheOauthToken().withCacheKey("OauthToken")
            .withConnection(new CacheConnection(cache)).withAccessTokenBuilder(new DummyAccessTokenBuilder(t));
    try {
      AdaptrisMessage m1 = new DefaultMessageFactory().newMessage(TEXT);
      LifecycleHelper.initAndStart(service);
      service.doService(m1);
      assertTrue(m1.headersContainsKey("Authorization"));
      assertEquals("Bearer " + getName(), m1.getMetadataValue("Authorization"));
      AccessToken t1 = (AccessToken) cache.get("OauthToken");
      assertNotNull(t1);
      AdaptrisMessage m2 = new DefaultMessageFactory().newMessage(TEXT);
      service.doService(m2);
      assertTrue(m2.headersContainsKey("Authorization"));
      assertEquals("Bearer " + getName(), m1.getMetadataValue("Authorization"));
      AccessToken t2 = (AccessToken) cache.get("OauthToken");
      assertNotNull(t2);
      assertSame(t1, t2);
    }
    finally {
      LifecycleHelper.stopAndClose(service);

    }
  }

  @Test(expected = ServiceException.class)
  public void testService_WithError() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache().withExpiration(new TimeInterval(5L, TimeUnit.SECONDS));
    AccessToken t = new AccessToken(getName());
    GetAndCacheOauthToken service = new GetAndCacheOauthToken().withCacheKey("OauthToken")
        .withConnection(new CacheConnection(cache)).withAccessTokenBuilder(new DummyAccessTokenBuilder(t, true));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    execute(service, msg);
  }

  @Test
  public void testService_Expiry() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache().withExpiration(new TimeInterval(5L, TimeUnit.SECONDS));
    AccessToken t = new AccessToken(getName());
    GetAndCacheOauthToken service = new GetAndCacheOauthToken().withCacheKey("OauthToken")
        .withConnection(new CacheConnection(cache)).withAccessTokenBuilder(new ExpiringAccessToken(t, 1000));
    try {
      AdaptrisMessage m1 = new DefaultMessageFactory().newMessage(TEXT);
      LifecycleHelper.initAndStart(service);
      service.doService(m1);
      AccessToken t1 = (AccessToken) cache.get("OauthToken");
      assertNotNull(t1);
      await()
          .atMost(Duration.ofSeconds(3))
          .with()
          .pollInterval(Duration.ofMillis(100))
          .until(() -> cache.size() == 0);
      AdaptrisMessage m2 = new DefaultMessageFactory().newMessage(TEXT);
      service.doService(m2);
      AccessToken t2 = (AccessToken) cache.get("OauthToken");
      assertNotNull(t2);
      assertNotSame(t1, t2);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Override
  protected GetAndCacheOauthToken retrieveObjectForSampleConfig() {
    return new GetAndCacheOauthToken()
        .withCacheKey("%message{cachekey}")
        .withAccessTokenWriter(new MetadataAccessTokenWriter())
        .withAccessTokenBuilder(new DummyAccessTokenBuilder());
  }

  private class ExpiringAccessToken implements AccessTokenBuilder {

    private transient long expiresIn;
    private transient AccessToken baseToken;

    private ExpiringAccessToken(AccessToken base, long expiryMs) {
      baseToken = base;
      expiresIn = expiryMs;
    }

    @Override
    public AccessToken build(AdaptrisMessage msg) throws IOException, CoreException {
      return rebuild();
    }

    private AccessToken rebuild() {
      AccessToken t = new AccessToken(baseToken.getType(), baseToken.getToken()).withRefreshToken(baseToken.getRefreshToken());
      t.setExpiry("" + expiresIn);
      return t;
    }
  }
}
