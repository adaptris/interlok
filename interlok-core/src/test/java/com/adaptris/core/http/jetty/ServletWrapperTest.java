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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServletWrapperTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHashCode() {
    ServletWrapper wrapper = new ServletWrapper(new MyServlet(), "/url");
    assertEquals(wrapper.hashCode(), wrapper.hashCode());
  }

  @Test
  public void testGetServletHolder() throws Exception {
    MyServlet servlet = new MyServlet();
    ServletWrapper wrapper = new ServletWrapper(servlet, "/url");
    assertNotNull(wrapper.getServletHolder());
    // As of jetty-all.9.4.22.v20191022 the internals of getServlet/setServlet has changed
    // Previously setServlet set _servlet to be what you passed in, and getServlet would return it
    // Now setServlet doesn't do that exactly, and getServlet returns you the initialised servlet.
    // which in this case is null (since we aren't initialising).
    // assertEquals(servlet, wrapper.getServletHolder().getServlet());
  }

  @Test
  public void testGetUrl() {
    ServletWrapper wrapper = new ServletWrapper(new MyServlet(), "/url");
    assertEquals("/url", wrapper.getUrl());
  }

  @Test
  public void testEqualsObject() {
    MyServlet servlet = new MyServlet();
    ServletWrapper wrapper = new ServletWrapper(servlet, "/url");
    assertTrue(wrapper.equals(wrapper));
    assertFalse(wrapper.equals(new Object()));
    assertFalse(wrapper.equals(null));
    ServletWrapper wrapper2 = new MyWrapper(wrapper.getServletHolder(), wrapper.getUrl());
    assertEquals(wrapper, wrapper2);
  }

  @SuppressWarnings("serial")
  private class MyServlet extends HttpServlet {

  }

  private class MyWrapper extends ServletWrapper {
    private ServletHolder servletHolder;
    private String url;

    private MyWrapper(ServletHolder s, String url) {
      super();
      this.servletHolder = s;
      this.url = url;
    }

    @Override
    public ServletHolder getServletHolder() {
      return servletHolder;
    }

    @Override
    public String getUrl() {
      return url;
    }
  }

}
