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
package com.adaptris.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdapterMarshallerFactory.MarshallingOutput;

public class AdapterXStreamMarshallerFactoryTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateMarshallerString() throws Exception {
    try {
      AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
      assertEquals(XStreamMarshaller.class, factory.createMarshaller().getClass());
      assertEquals(XStreamMarshaller.class, factory.createMarshaller(MarshallingOutput.XML).getClass());
      assertEquals(XStreamJsonMarshaller.class, factory.createMarshaller("JSON").getClass());
      assertEquals(XStreamMarshaller.class, factory.createMarshaller("").getClass());
      assertEquals(XStreamMarshaller.class, factory.createMarshaller("Hello").getClass());
    } finally {
      AdapterXStreamMarshallerFactory.reset();
    }
  }

  @Test
  public void testCreateMarshaller_Null() throws Exception {
    Assertions.assertThrows(NullPointerException.class, () -> {
      try {
        AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
        factory.createMarshaller((MarshallingOutput) null);
      } finally {
        AdapterXStreamMarshallerFactory.reset();
      }
    });
  }

  @Test
  public void testCreateXStream() throws Exception {
    try {
      AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
      assertNotNull(factory.createXStreamInstance(MarshallingOutput.XML));
      assertNotNull(factory.createXStreamInstance(MarshallingOutput.JSON));
    } finally {
      AdapterXStreamMarshallerFactory.reset();
    }
  }

  @Test
  public void testCreateXStream_Modes() throws Exception {
    try {
      AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
      assertNotNull(factory.createXStreamInstance(MarshallingOutput.XML));
      assertNotNull(factory.createXStreamInstance(MarshallingOutput.JSON));
    } finally {
      AdapterXStreamMarshallerFactory.reset();
    }
  }

  @Test
  public void testCreateXStream_null() throws Exception {
    Assertions.assertThrows(NullPointerException.class, () -> {
      try {
        AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
        factory.createXStreamInstance((MarshallingOutput) null);
      } finally {
        AdapterXStreamMarshallerFactory.reset();
      }
    });
  }
}
