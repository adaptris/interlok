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
package com.adaptris.core.security.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.security.access.MetadataIdentityBuilderImpl.MetadataSource;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;

public class IdentityBuilderTest extends BaseCase {

  public static final String MAPPED_USER = "mappedU";
  public static final String MAPPED_PASSWORD = "mappedP";
  public static final String MAPPED_ROLE = "mappedR";
  public static final String USER = "user";
  public static final String PASSWORD = "password";
  public static final String ROLE = "role";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testEmptyIdentityBuilder() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(USER, getName() + ThreadLocalRandom.current().nextInt());
    msg.addMetadata(PASSWORD, getName() + ThreadLocalRandom.current().nextInt());
    msg.addMetadata(ROLE, getName() + ThreadLocalRandom.current().nextInt());
    EmptyIdentityBuilder builder = new EmptyIdentityBuilder();
    assertEquals(0, builder.build(msg).size());
  }

  @Test
  public void testEmptyIdentityBuilder_RoundTrip() throws Exception {
    EmptyIdentityBuilder builder = new EmptyIdentityBuilder();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    assertRoundtripEquality(builder, m.unmarshal(m.marshal(builder)));
  }

  @Test
  public void testMetadataIdentityBuilder() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String username = getName() + ThreadLocalRandom.current().nextInt();
    String password = getName() + ThreadLocalRandom.current().nextInt();
    String role = getName() + +ThreadLocalRandom.current().nextInt();
    msg.addMetadata(USER, username);
    msg.addMetadata(PASSWORD, password);
    msg.addMetadata(ROLE, role);
    MetadataIdentityBuilder builder = new MetadataIdentityBuilder(MetadataSource.Standard, Arrays.asList(USER, PASSWORD, ROLE));
    try {
      BaseCase.start(builder);
      Map<String, Object> identityMap = builder.build(msg);
      assertTrue(identityMap.containsKey(ROLE));
      assertTrue(identityMap.containsKey(USER));
      assertTrue(identityMap.containsKey(PASSWORD));
      assertEquals(username, identityMap.get(USER));
      assertEquals(password, identityMap.get(PASSWORD));
      assertEquals(role, identityMap.get(ROLE));
    }
    finally {
      BaseCase.stop(builder);
    }
  }

  @Test
  public void testMetadataIdentityBuilder_ObjectMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String username = getName() + ThreadLocalRandom.current().nextInt();
    String password = getName() + ThreadLocalRandom.current().nextInt();
    String role = getName() + +ThreadLocalRandom.current().nextInt();
    msg.getObjectHeaders().put(USER, username);
    msg.getObjectHeaders().put(PASSWORD, password);
    msg.getObjectHeaders().put(ROLE, role);
    MetadataIdentityBuilder builder = new MetadataIdentityBuilder(MetadataSource.Object, Arrays.asList(USER, PASSWORD, ROLE));
    try {
      BaseCase.start(builder);
      Map<String, Object> identityMap = builder.build(msg);
      assertTrue(identityMap.containsKey(ROLE));
      assertTrue(identityMap.containsKey(USER));
      assertTrue(identityMap.containsKey(PASSWORD));
      assertEquals(username, identityMap.get(USER));
      assertEquals(password, identityMap.get(PASSWORD));
      assertEquals(role, identityMap.get(ROLE));
    }
    finally {
      BaseCase.stop(builder);
    }
  }

  @Test
  public void testMetadataIdentityBuilder_RoundTrip() throws Exception {
    MetadataIdentityBuilder builder = new MetadataIdentityBuilder(MetadataSource.Standard, Arrays.asList(USER, PASSWORD, ROLE));
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    assertRoundtripEquality(builder, m.unmarshal(m.marshal(builder)));
  }

  @Test
  public void testMappedMetadataIdentityBuilder() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String username = getName() + ThreadLocalRandom.current().nextInt();
    String password = getName() + ThreadLocalRandom.current().nextInt();
    String role = getName() + +ThreadLocalRandom.current().nextInt();
    msg.addMetadata(MAPPED_USER, username);
    msg.addMetadata(MAPPED_PASSWORD, password);
    msg.addMetadata(MAPPED_ROLE, role);
    MappedMetadataIdentityBuilder builder = new MappedMetadataIdentityBuilder(new KeyValuePairList(Arrays.asList(new KeyValuePair[]
    {
        new KeyValuePair(MAPPED_USER, USER), new KeyValuePair(MAPPED_PASSWORD, PASSWORD), new KeyValuePair(MAPPED_ROLE, ROLE)
    })));
    try {
      BaseCase.start(builder);
      Map<String, Object> identityMap = builder.build(msg);
      assertTrue(identityMap.containsKey(ROLE));
      assertTrue(identityMap.containsKey(USER));
      assertTrue(identityMap.containsKey(PASSWORD));
      assertEquals(username, identityMap.get(USER));
      assertEquals(password, identityMap.get(PASSWORD));
      assertEquals(role, identityMap.get(ROLE));
    }
    finally {
      BaseCase.stop(builder);
    }
  }

  @Test
  public void testMappedMetadataIdentityBuilder_ObjectMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String username = getName() + ThreadLocalRandom.current().nextInt();
    String password = getName() + ThreadLocalRandom.current().nextInt();
    String role = getName() + +ThreadLocalRandom.current().nextInt();
    msg.getObjectHeaders().put(MAPPED_USER, username);
    msg.getObjectHeaders().put(MAPPED_PASSWORD, password);
    msg.getObjectHeaders().put(MAPPED_ROLE, role);
    MappedMetadataIdentityBuilder builder = new MappedMetadataIdentityBuilder(MetadataSource.Object,
        new KeyValuePairList(Arrays.asList(new KeyValuePair[]
        {
            new KeyValuePair(MAPPED_USER, USER), new KeyValuePair(MAPPED_PASSWORD, PASSWORD), new KeyValuePair(MAPPED_ROLE, ROLE)
        })));
    try {
      BaseCase.start(builder);
      Map<String, Object> identityMap = builder.build(msg);
      assertTrue(identityMap.containsKey(ROLE));
      assertTrue(identityMap.containsKey(USER));
      assertTrue(identityMap.containsKey(PASSWORD));
      assertEquals(username, identityMap.get(USER));
      assertEquals(password, identityMap.get(PASSWORD));
      assertEquals(role, identityMap.get(ROLE));
    }
    finally {
      BaseCase.stop(builder);
    }
  }

  @Test
  public void testMappedMetadataIdentityBuilder_RoundTrip() throws Exception {
    MappedMetadataIdentityBuilder builder = new MappedMetadataIdentityBuilder(MetadataSource.Standard,
        new KeyValuePairList(Arrays.asList(new KeyValuePair[]
        {
            new KeyValuePair(MAPPED_USER, USER), new KeyValuePair(MAPPED_PASSWORD, PASSWORD), new KeyValuePair(MAPPED_ROLE, ROLE)
        })));
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    assertRoundtripEquality(builder, m.unmarshal(m.marshal(builder)));
  }

  @Test
  public void testCompositeIdentityBuilder() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String username = getName() + ThreadLocalRandom.current().nextInt();
    String password = getName() + ThreadLocalRandom.current().nextInt();
    String role = getName() + +ThreadLocalRandom.current().nextInt();
    msg.addMetadata(MAPPED_USER, username);
    msg.addMetadata(MAPPED_PASSWORD, password);
    msg.addMetadata(ROLE, role);
    MappedMetadataIdentityBuilder mapped = new MappedMetadataIdentityBuilder(new KeyValuePairList(Arrays.asList(new KeyValuePair[]
    {
        new KeyValuePair(MAPPED_USER, USER), new KeyValuePair(MAPPED_PASSWORD, PASSWORD)
    })));
    MetadataIdentityBuilder metadata = new MetadataIdentityBuilder(Arrays.asList(USER, PASSWORD, ROLE));
    // Due to order, we should get the mapped-user becoming the "user" etc.
    CompositeIdentityBuilder builder = new CompositeIdentityBuilder(
        new ArrayList<IdentityBuilder>(Arrays.asList(metadata, mapped)));
    try {
      BaseCase.start(builder);
      Map<String, Object> identityMap = builder.build(msg);
      assertTrue(identityMap.containsKey(ROLE));
      assertTrue(identityMap.containsKey(USER));
      assertTrue(identityMap.containsKey(PASSWORD));
      assertEquals(username, identityMap.get(USER));
      assertEquals(password, identityMap.get(PASSWORD));
      assertEquals(role, identityMap.get(ROLE));
    }
    finally {
      BaseCase.stop(builder);
    }
  }

  @Test
  public void testCompositeMetadataIdentityBuilder_RoundTrip() throws Exception {
    MappedMetadataIdentityBuilder mapped = new MappedMetadataIdentityBuilder(new KeyValuePairList(Arrays.asList(new KeyValuePair[]
    {
        new KeyValuePair(MAPPED_USER, USER), new KeyValuePair(MAPPED_PASSWORD, PASSWORD)
    })));
    MetadataIdentityBuilder metadata = new MetadataIdentityBuilder(Arrays.asList(USER, PASSWORD, ROLE));
    // Due to order, we should get the mapped-user becoming the "user" etc.
    CompositeIdentityBuilder builder = new CompositeIdentityBuilder(
        new ArrayList<IdentityBuilder>(Arrays.asList(metadata, mapped)));
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    assertRoundtripEquality(builder, m.unmarshal(m.marshal(builder)));
  }
}
