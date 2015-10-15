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

package com.adaptris.core;


public class MetadataFileNameCreatorTest extends BaseCase {

  private AdaptrisMessage msg;
  public MetadataFileNameCreatorTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key", "value");
    msg.addMetadata("key2", "");

  }

  public void testSetMetadataKey() throws Exception {
    MetadataFileNameCreator fnc = new MetadataFileNameCreator("key");
    try {
      fnc.setMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      fnc.setMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testSetDefaultName() throws Exception {
    MetadataFileNameCreator fnc = new MetadataFileNameCreator("key");
    try {
      fnc.setDefaultName(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      fnc.setDefaultName("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testCreateName() throws CoreException {
    MetadataFileNameCreator fnc = new MetadataFileNameCreator("key");
    assertEquals("value", fnc.createName(msg));
  }

  public void testCreateNameDefault() throws CoreException {
    MetadataFileNameCreator fnc = new MetadataFileNameCreator("non existent key");
    assertEquals("MetadataFileNameCreator_default", fnc.createName(msg));
  }

  public void testCreateNameNullKey() throws CoreException {
    MetadataFileNameCreator fnc = new MetadataFileNameCreator();
    try {
      fnc.createName(msg);
      fail("no Exception from null key");
    }
    catch (CoreException e) {
      // expected behaviour...
    }
  }

  public void testCreateNameKeyReturnsEmptyString() throws CoreException {
    MetadataFileNameCreator fnc = new MetadataFileNameCreator("key2");
    assertEquals("MetadataFileNameCreator_default", fnc.createName(msg));
  }


  public void testXmlRoundTrip() throws Exception {
    MetadataFileNameCreator input = new MetadataFileNameCreator("key", "default");
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    MetadataFileNameCreator output = (MetadataFileNameCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}
