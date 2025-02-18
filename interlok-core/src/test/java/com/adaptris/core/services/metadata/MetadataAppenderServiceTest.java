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

package com.adaptris.core.services.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MetadataAppenderServiceTest extends MetadataServiceExample {

  private MetadataAppenderService service;
  private String resultKey;
  private AdaptrisMessage msg;


  @BeforeEach
  public void setUp() throws Exception {
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "val1");
    msg.addMetadata("key2", "val2");
    msg.addMetadata("key3", "val3");

    resultKey = "result";

    service = new MetadataAppenderService();
    service.setResultKey(resultKey);
  }

  @Test
  public void testSetEmptyResultKey() throws Exception {
    MetadataAppenderService service = new MetadataAppenderService();
    try {
      service.setResultKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }

  }

  @Test
  public void testSetNullResultKey() throws Exception {
    MetadataAppenderService service = new MetadataAppenderService();
    try {
      service.setResultKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }

  }

  @Test
  public void testZeroKeys() throws CoreException {
    service.getAppendKeys().clear();
    service.setSeparator("|");

    execute(service, msg);
    assertEquals("", msg.getMetadataValue(resultKey));
  }

  @Test
  public void testTwoKeys() throws CoreException {
    service.addAppendKey("key1");
    service.addAppendKey("key3");

    execute(service, msg);
    assertTrue("val1val3".equals(msg.getMetadataValue(resultKey)));
  }

  @Test
  public void testTwoReferencedKeys() throws CoreException {
    msg.addMessageHeader("RefKey1", "key1");
    msg.addMessageHeader("RefKey3", "key3");
    
    service.addAppendKey("$$RefKey1");
    service.addAppendKey("$$RefKey3");

    execute(service, msg);
    assertTrue("val1val3".equals(msg.getMetadataValue(resultKey)));
  }

  @Test
  public void testTwoKeysOneNotSet() throws CoreException {
    service.addAppendKey("key1");
    service.addAppendKey("key4");

    execute(service, msg);
    assertTrue("val1".equals(msg.getMetadataValue(resultKey)));
  }

  @Test
  public void testNullKey() throws CoreException {
    try {
      service.addAppendKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // okay
    }
  }

  @Test
  public void testEmptyKey() throws CoreException {
    try {
      service.addAppendKey("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // okay
    }
  }

  @ParameterizedTest
  @MethodSource("testSeparatorProvider")
  public void testSeparator(String... args) throws CoreException {
    service.setSeparator("|");
    List<String> argsList = Arrays.stream(args).toList();
    List<String> keys = argsList.subList(0, argsList.size() - 1);
    String expected = argsList.get(argsList.size() - 1);
    keys.stream().forEach(key -> service.addAppendKey(key));
    execute(service, msg);
    assertEquals(expected, msg.getMetadataValue(resultKey));
  }

  @Test
  public void testNullSeparator() throws CoreException {
    service.setSeparator(null);
    assertEquals(MetadataAppenderService.DEFAULT_SEPARATOR, service.getSeparator());
  }

  static Stream<Arguments> testSeparatorProvider() {
    return Stream.of(
            Arguments.of((Object) new String[]{"key1", "val1"}),
            Arguments.of((Object) new String[]{"key1", "key2", "val1|val2"}),
            Arguments.of((Object) new String[]{"key1", "key2", "key3", "val1|val2|val3"}),
            Arguments.of((Object) new String[]{"key1", "key2", "key3", "key4", "val1|val2|val3"})
    );
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.addAppendKey("key1");
    service.addAppendKey("key2");

    return service;
  }
}
