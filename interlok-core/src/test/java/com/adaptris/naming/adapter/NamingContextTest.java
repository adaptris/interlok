/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.naming.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.management.SystemPropertiesUtil;

public class NamingContextTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  private CloseableNamingContext closeableContext() {
    Hashtable<String, Object> env = new Hashtable();
    env.put(Context.URL_PKG_PREFIXES, SystemPropertiesUtil.NAMING_PACKAGE);
    Map<String, Object> bindings = new HashMap<>();
    bindings.put(this.getClass().getSimpleName(), new Object());
    return new CloseableNamingContext(env, bindings);
  }

  private Name createName(String s) throws InvalidNameException {
    return new CompositeName(s);
  }

  @Test
  public void testBind() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.bind("hello", new Object());
      assertNotNull(context.lookup("hello"));
      context.bind("comp/env/hello", new Object());
      context.bind("testBind", new Object());
      context.createSubcontext("testBindContext");
      assertNotNull(context.lookup("comp/env/hello"));
      try {
        context.bind(new CompositeName(), new Object());
        fail();
      }
      catch (NamingException expected) {

      }
      try {
        context.bind("comp/env/hello", new Object());
        fail();
      }
      catch (NameAlreadyBoundException expected) {

      }
      try {
        context.bind("testBind/object", new Object());
        fail();
      }
      catch (NamingException expected) {

      }
      context.bind(createName("testBindContext/object"), new Object(), false);
      context.unbind("testBindContext/object");
      context.bind(createName("testBindContext/object"), new Object(), true);
    }
  }

  @Test
  public void testBindNameObjectBoolean() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      Object bindObj = new Object();
      context.bind(createName("hello"), bindObj, true);
      assertNotNull(context.lookup("hello"));
      assertEquals(bindObj, context.lookup("hello"));
      context.bind(createName("hello"), new Object(), true);
      assertNotNull(context.lookup("hello"));
      assertNotSame(bindObj, context.lookup("hello"));
    }
  }

  @Test
  public void testComposeNameNameName() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      Name c = context.composeName(createName("world"), createName("hello"));
      assertEquals("hello/world", c.toString());
    }
  }

  @Test
  public void testComposeNameStringString() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      assertEquals("hello/world", context.composeName("world", "hello"));
    }
  }

  @Test
  public void testCreateSubcontext() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      Context sub = context.createSubcontext("hello");
      assertNotNull(context.lookup("hello"));
    }
  }

  @Test
  public void testDestroySubcontext() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.bind("goodbye", new Object());
      context.bind("a/b/c", new Object());
      context.createSubcontext(createName("hello"));
      assertNotNull(context.lookup("hello"));
      context.destroySubcontext(createName("hello"));
      try {
        context.destroySubcontext("hello");
        fail();
      }
      catch (NameNotFoundException exoected) {

      }
      try {
        context.destroySubcontext("a/b/c");
        fail();
      }
      catch (NamingException exoected) {

      }
      context.destroySubcontext("a/b");
      try {
        context.destroySubcontext("goodbye");
        fail();
      }
      catch (NotContextException expected) {

      }
      try {
        context.destroySubcontext(new CompositeName());
        fail();
      }
      catch (NamingException expected) {

      }
    }
  }

  @Test
  public void testGetNameInNamespace() throws Exception {
    try (CloseableNamingContext context = new CloseableNamingContext(null, "name")) {
      assertEquals("name", context.getNameInNamespace());
    }
  }

  @Test
  public void testGetNameParserName() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      NameParser p = context.getNameParser(createName("blah"));
      assertEquals(createName("hello"), p.parse("hello"));
    }
  }

  @Test
  public void testGetNameParserString() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      NameParser p = context.getNameParser("hello");
      assertEquals(createName("hello"), p.parse("hello"));
    }
  }

  @Test
  public void testList() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.bind(createName("hello"), context, true);
      context.createSubcontext(createName("world"));
      try (CloseableEnumeration e = new CloseableEnumeration(context.list("hello"))) {
        while (e.hasMore()) {
          e.next();
        }
      }
      try (CloseableEnumeration e = new CloseableEnumeration(context.list("hello"))) {
        while (e.hasMoreElements()) {
          e.nextElement();
        }
      }
      context.list("world");
      context.bind(createName("NotContext"), new Object());
      try {
        context.list("NotContext");
        fail();
      }
      catch (NotContextException exc) {

      }
    }
  }

  @Test
  public void testListBindings() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.bind(createName("hello"), context, true);
      context.createSubcontext(createName("world"));
      try (CloseableEnumeration e = new CloseableEnumeration(context.listBindings("hello"))) {
        while (e.hasMore()) {
          e.next();
        }
      }
      try (CloseableEnumeration e = new CloseableEnumeration(context.listBindings("hello"))) {
        while (e.hasMoreElements()) {
          e.nextElement();
        }
      }
      context.listBindings("world");
      context.bind(createName("NotContext"), new Object());
      try {
        context.listBindings("NotContext");
        fail();
      }
      catch (NotContextException exc) {

      }
    }
  }

  @Test
  public void testLookup() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.bind("hello", new Object());
      assertNotNull(context.lookup(""));
      assertNotNull(context.lookupLink("hello"));
      try {
        assertNotNull(context.lookup("adapter:/comp/env/object"));
      }
      catch (NameNotFoundException expected) {

      }
      try {
        context.lookup("zzzz:/comp/env/object");
      }
      catch (NamingException expected) {

      }
    }
  }

  @Test
  public void testUnbind() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.bind("hello", new Object());
      context.unbind("hello");
      try {
        context.lookup("hello");
        fail();
      }
      catch (NamingException expected) {

      }
      try {
        context.unbind("hello");
        fail();
      }
      catch (NameNotFoundException expected) {

      }
      try {
        context.unbind(new CompositeName());
        fail();
      }
      catch (NamingException expected) {

      }
    }
  }

  @Test
  public void testRebindStringObject() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.rebind("hello", new Object());
      assertNotNull(context.lookup("hello"));
    }
  }

  @Test
  public void testEnvironment() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.addToEnvironment("hello", "world");
      Hashtable env = context.getEnvironment();
      assertTrue(env.containsKey("hello"));
      context.removeFromEnvironment("hello");
      assertFalse(context.getEnvironment().containsKey("hello"));
    }
  }

  @Test
  public void testRenameStringString() throws Exception {
    try (CloseableNamingContext context = closeableContext()) {
      context.bind("hello", new Object());
      context.rename("hello", "world");
      try {
        context.lookup("hello");
        fail();
      }
      catch (NamingException expected) {

      }
      context.lookup("world");
    }
  }

  @SuppressWarnings("serial")
  private class CloseableNamingContext extends NamingContext implements Closeable {

    public CloseableNamingContext(Hashtable<String, Object> environment, Map<String, Object> objects) {
      super(environment, objects);
    }

    public CloseableNamingContext(Hashtable<String, Object> environment, String nameInNamespace) {
      super(environment, nameInNamespace);
    }

    @Override
    public void close() {
      try {
        super.close();
      }
      catch (Exception e) {

      }
    }
  }

  private class CloseableEnumeration implements NamingEnumeration, Closeable {
    private NamingEnumeration proxy;

    private CloseableEnumeration(NamingEnumeration ne) {
      proxy = ne;
    }

    @Override
    public boolean hasMoreElements() {
      return proxy.hasMoreElements();
    }

    @Override
    public Object nextElement() {
      return proxy.nextElement();
    }

    @Override
    public Object next() throws NamingException {
      return proxy.next();
    }

    @Override
    public boolean hasMore() throws NamingException {
      return proxy.hasMore();
    }

    @Override
    public void close() {
      try {
        proxy.close();
      }
      catch (Exception e) {

      }
    }
  }
}
