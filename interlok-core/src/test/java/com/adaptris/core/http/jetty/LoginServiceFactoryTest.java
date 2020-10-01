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

package com.adaptris.core.http.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.JDBCLoginService;
import org.eclipse.jetty.security.LoginService;
import org.junit.Test;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class LoginServiceFactoryTest {


  @Test
  public void testCreateHashLoginService() throws Exception {
    // So we don't fall foul of INTERLOK-2902
    String realm = BaseCase.PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM);
    HashLoginServiceFactory factory =
        new HashLoginServiceFactory().withUserRealm("InterlokJetty").withFilename(realm);
    assertEquals("InterlokJetty", factory.getUserRealm());
    assertNotNull(factory.getFilename());
    LoginService loginService = factory.retrieveLoginService();
    assertNotNull(loginService);
    assertTrue(loginService instanceof LoginServiceProxy);
    LoginServiceProxy proxy = (LoginServiceProxy) loginService;
    assertEquals(1, proxy.getBeans().size());
    assertEquals(HashLoginService.class, proxy.getBeans().iterator().next().getClass());
  }

  @Test
  public void testCreateJdbcLoginService() throws Exception {
    // while the realm.properties doesn't actually contain the right info, it will
    // exist so we don't fall foul of INTERLOK-2902
    String realm = BaseCase.PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM);
    JdbcLoginServiceFactory factory =
        new JdbcLoginServiceFactory().withUserRealm("InterlokJetty").withFilename(realm);
    assertEquals("InterlokJetty", factory.getUserRealm());
    assertNotNull(factory.getFilename());
    LoginService loginService = factory.retrieveLoginService();
    assertNotNull(loginService);
    assertTrue(loginService instanceof LoginServiceProxy);
    LoginServiceProxy proxy = (LoginServiceProxy) loginService;
    assertEquals(1, proxy.getBeans().size());
    assertEquals(JDBCLoginService.class, proxy.getBeans().iterator().next().getClass());
  }

}
