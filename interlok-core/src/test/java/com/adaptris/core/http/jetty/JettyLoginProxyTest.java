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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.server.UserIdentity;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author lchan
 *
 */
public class JettyLoginProxyTest {

  @Test
  public void testLogin_HasID() throws Exception {
    LoginServiceProxy proxy = createProxy(true);
    ServletRequest request = Mockito.mock(ServletRequest.class);
    try {
      proxy.start();
      assertNotNull(proxy.login("hello", "world", request));
    } finally {
      proxy.stop();
    }
  }

  @Test
  public void testLogin_NoID() throws Exception {
    LoginServiceProxy proxy = createProxy(false);
    ServletRequest request = Mockito.mock(ServletRequest.class);
    try {
      proxy.start();
      assertNull(proxy.login("hello", "world", request));
    } finally {
      proxy.stop();
    }
  }

  @Test
  public void testValidate() throws Exception {
    LoginServiceProxy proxy = createProxy(true);
    try {
      proxy.start();
      // Just pass through...
      assertTrue(proxy.validate(null));
    } finally {
      proxy.stop();
    }
  }

  @Test
  public void testIdentityService() throws Exception {
    LoginServiceProxy proxy = createProxy(true);
    proxy.setIdentityService(new DefaultIdentityService());
    assertEquals(DefaultIdentityService.class, proxy.getIdentityService().getClass());
  }

  @Test
  public void testLogout() throws Exception {
    LoginServiceProxy proxy = createProxy(true);
    try {
      proxy.start();
      proxy.logout(null);
    } finally {
      proxy.stop();
    }
  }

  private LoginServiceProxy createProxy(boolean withUserIdentity) {
    UserIdentity id = null;
    if (withUserIdentity) {
      KnownUser principal = new KnownUser("username");
      Subject subject = new Subject();
      subject.getPrincipals().add(principal);
      subject.getPrivateCredentials().add(new Object());
      id = new DefaultUserIdentity(subject, principal, new String[] {
          "role1", "role2", "role3"
      });
    }
    return new LoginServiceProxy().withLoginService(new MyLoginService(id));
  }

  private class MyLoginService extends AbstractLoginService {

    private UserIdentity myId;
    private IdentityService identityService;

    private MyLoginService(UserIdentity id) {
      myId = id;
    }

    @Override
    public UserIdentity login(String username, Object credentials, ServletRequest request) {
      return myId;
    }

    @Override
    public boolean validate(UserIdentity user) {
      return true;
    }

    @Override
    public IdentityService getIdentityService() {
      return identityService;
    }

    @Override
    public void setIdentityService(IdentityService service) {
      identityService = service;
    }

    @Override
    public void logout(UserIdentity user) {
    }

    @Override
    protected String[] loadRoleInfo(UserPrincipal user) {
      // Never called since we override all the LoginService imps
      return new String[0];
    }

    @Override
    protected UserPrincipal loadUserInfo(String username) {
      // Never called since we override all the LoginService imps
      return null;
    }

  }

  private class KnownUser implements Principal {

    private String username;

    public KnownUser(String username) {
      this.username = username;
    }

    @Override
    public String getName() {
      return username;
    }

    public boolean authenticate(KnownUser user) {
      return true;
    }

    public boolean checkCredentials(Object credentials) {
        return true;
    }

  }
}
