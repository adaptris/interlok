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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.JndiContextFactory;

public class ContextFactoryTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  private Context createContext() throws NamingException {
    JndiContextFactory factory = new JndiContextFactory();
    return factory.getInitialContext(new Hashtable<>());
  }

  @Test
  public void testGetObjectInstance_String() throws Exception {
    Context ctx = createContext();
    Object bindObject = new Object();
    ctx.bind("testGetObjectInstance_String", bindObject);
    adapterURLContextFactory factory = new adapterURLContextFactory();
    assertEquals(bindObject, factory.getObjectInstance("testGetObjectInstance_String", null, null, new Hashtable()));
    ctx.unbind("testGetObjectInstance_String");
  }

  @Test
  public void testGetObjectInstance_Object() throws Exception {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      adapterURLContextFactory factory = new adapterURLContextFactory();
      factory.getObjectInstance(new Object(), null, null, new Hashtable());
    });
  }

  @Test
  public void testGetObjectInstance_EmptyStringArray() throws Exception {
    Assertions.assertThrows(ConfigurationException.class, () -> {
      adapterURLContextFactory factory = new adapterURLContextFactory();
      factory.getObjectInstance(new String[0], null, null, new Hashtable());
    });
  }

  @Test
  public void testGetObjectInstance_StringArray() throws Exception {
    Context ctx = createContext();
    Object bindObject = new Object();
    ctx.bind("testGetObjectInstance_StringArray", bindObject);
    adapterURLContextFactory factory = new adapterURLContextFactory();
    assertEquals(bindObject,
        factory.getObjectInstance(
            new String[] { "adapter:comp/env/xxx", "adapter:comp/env/yyy", "testGetObjectInstance_StringArray" }, null,
            null, new Hashtable()));
  }

}
