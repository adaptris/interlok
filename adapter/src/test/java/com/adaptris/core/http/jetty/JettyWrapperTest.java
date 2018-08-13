/*
 * Copyright 2018 Adaptris Ltd.
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class JettyWrapperTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testUnwrap() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    JettyWrapper wrapper = new JettyWrapper();
    msg.addObjectHeader(JettyConstants.JETTY_WRAPPER, wrapper);
    assertEquals(wrapper, JettyWrapper.unwrap(msg));
    assertNotSame(wrapper, JettyWrapper.unwrap(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }

  @Test
  public void testMonitor() {
    JettyWrapper wrapper = new JettyWrapper();
    assertNotNull(wrapper.getMonitor());
    JettyConsumerMonitor monitor = wrapper.getMonitor();
    wrapper.withMonitor(new JettyConsumerMonitor());
    assertNotSame(monitor, wrapper.getMonitor());
  }

  @Test
  public void testResponse() {
    JettyWrapper wrapper = new JettyWrapper();
    assertNull(wrapper.getResponse());
    assertNotNull(wrapper.withResponse(createDummyResponse()).getResponse());
  }

  @Test
  public void testRequest() {
    JettyWrapper wrapper = new JettyWrapper();
    assertNull(wrapper.getRequest());
    assertNotNull(wrapper.withRequest(createDummyRequest()).getRequest());
  }


  // cos you know, reflection is awesome.
  private HttpServletResponse createDummyResponse() {
    HttpServletResponse dummy = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
        new java.lang.Class[]
        {
            HttpServletResponse.class
        }, new NoOpInvocationHandler());
    return dummy;
  }

  private HttpServletRequest createDummyRequest() {
    HttpServletRequest dummy = (HttpServletRequest) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
        new java.lang.Class[]
        {
            HttpServletRequest.class
        }, new NoOpInvocationHandler());
    return dummy;
  }

  private class NoOpInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return null;
    }

  }
}
