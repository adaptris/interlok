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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.adaptris.util.GuidGenerator;

public class MessageLifecycleEventTest {

  private static final String MLEMARKER_NAME = "mlemarker";
  private static final String DEFAULT_ID = "ABCDEFG";

  @Test
  public void testGetNamespaceSuccess() {
    MessageLifecycleEvent mle = new MessageLifecycleEvent();
    assertEquals(EventNameSpaceConstants.MESSAGE_LIFECYCLE + ".success", mle.getNameSpace());
  }

  @Test
  public void testGetNamespaceFail() {
    MessageLifecycleEvent mle = new MessageLifecycleEvent();
    mle.addMleMarker( new MleMarker(MLEMARKER_NAME, true, 0, "0"));
    mle.addMleMarker(new MleMarker(MLEMARKER_NAME, false, 1, "0"));
    assertEquals(EventNameSpaceConstants.MESSAGE_LIFECYCLE + ".fail", mle.getNameSpace());
  }

  @Test
  public void testAddMleMarker() throws Exception {
    MessageLifecycleEvent mle = new MessageLifecycleEvent();
    MleMarker marker = new MleMarker(MLEMARKER_NAME, true, 0, new GuidGenerator().create(new Object()));
    mle.addMleMarker(marker);
    try {
      mle.addMleMarker(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(1, mle.getMleMarkers().size());
    assertEquals(marker, mle.getMleMarkers().get(0));
  }

  @Test
  public void testSetMleMarkers() throws Exception {
    MessageLifecycleEvent mle = new MessageLifecycleEvent();
    List list = Arrays.asList(new MleMarker[]
    {
        new MleMarker(MLEMARKER_NAME, true, 0, new GuidGenerator().create(new Object())),
        new MleMarker(MLEMARKER_NAME, true, 1, new GuidGenerator().create(new Object())),
    });
    mle.setMleMarkers(list);
    assertEquals(2, mle.getMleMarkers().size());
    assertEquals(list, mle.getMleMarkers());
  }

  @Test
  public void testSetMessageUniqueId() throws Exception {
    MessageLifecycleEvent mle = new MessageLifecycleEvent();
    mle.setMessageUniqueId(DEFAULT_ID);
    try {
      mle.setMessageUniqueId(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(0, mle.getMleMarkers().size());
    assertEquals(DEFAULT_ID, mle.getMessageUniqueId());
  }

  @Test
  public void testSetChannelId() throws Exception {
    MessageLifecycleEvent mle = new MessageLifecycleEvent();
    mle.setChannelId(DEFAULT_ID);
    assertEquals(mle.toString(), 0, mle.getMleMarkers().size());
    assertEquals(mle.toString(), DEFAULT_ID, mle.getChannelId());
  }

  @Test
  public void testSetWorkflowId() throws Exception {
    MessageLifecycleEvent mle = new MessageLifecycleEvent();
    mle.setWorkflowId(DEFAULT_ID);
    assertEquals(mle.toString(), 0, mle.getMleMarkers().size());
    assertEquals(mle.toString(), DEFAULT_ID, mle.getWorkflowId());
  }

  @Test
  public void testMleMarkerSetName() throws Exception {
    MleMarker m = new MleMarker();
    m.setName(DEFAULT_ID);
    try {
      m.setName(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(m.toString(), DEFAULT_ID, m.getName());
  }

  @Test
  public void testMleMarkerSetCreationTime() throws Exception {
    MleMarker m = new MleMarker();
    m.setCreationTime(0);
    assertEquals(m.toString(), 0, m.getCreationTime());
  }

  @Test
  public void testMleMarkerSetSequenceNumber() throws Exception {
    MleMarker m = new MleMarker();
    m.setSequenceNumber(0);
    assertEquals(m.toString(), 0, m.getSequenceNumber());
  }

  @Test
  public void testMleMarkerSetWasSuccessful() throws Exception {
    MleMarker m = new MleMarker();
    m.setWasSuccessful(false);
    assertEquals(m.toString(), false, m.getWasSuccessful());
  }

  @Test
  public void testMleMarkerSetUniqueId() throws Exception {
    MleMarker m = new MleMarker();
    m.setUniqueId(DEFAULT_ID);
    assertEquals(m.toString(), DEFAULT_ID, m.getUniqueId());
  }

  @Test
  public void testMleMarkerSetIsTrackingEndpoint() throws Exception {
    MleMarker m = new MleMarker();
    m.setIsTrackingEndpoint(true);
    assertEquals(m.toString(), true, m.getIsTrackingEndpoint());
  }

  @Test
  public void testMleMarkerSetIsConfirmation() throws Exception {
    MleMarker m = new MleMarker();
    m.setIsConfirmation(true);
    assertEquals(m.toString(), true, m.getIsConfirmation());
  }

  @Test
  public void testMleMarkerSetConfirmationId() throws Exception {
    MleMarker m = new MleMarker();
    m.setConfirmationId(DEFAULT_ID);
    assertEquals(m.toString(), DEFAULT_ID, m.getConfirmationId());
  }

  @Test
  public void testMleMarkerClone() throws Exception {
    MleMarker m = new MleMarker(MLEMARKER_NAME, true, 0, DEFAULT_ID);
    MleMarker m2 = (MleMarker) m.clone();
    assertEquals(m.toString(), m, m2);
    assertFalse(m == m2);
  }

  @Test
  public void testMleMarkerEquality() throws Exception {
    MleMarker m1 = new MleMarker(MLEMARKER_NAME, true, 0, new GuidGenerator().create(new Object()));
    MleMarker m2 = new MleMarker(MLEMARKER_NAME, false, 0, new GuidGenerator().create(new Object()));
    MleMarker m3 = new MleMarker(MLEMARKER_NAME, true, 1, new GuidGenerator().create(new Object()));
    MleMarker m4 = new MleMarker("mlemarker5", true, 0, new GuidGenerator().create(new Object()));
    assertEquals(m1, m1);
    assertTrue(m1.equals(m1));
    assertEquals(m1.hashCode(), m1.hashCode());
    assertNotSame(m1, m2);
    assertFalse(m1.equals(m2));
    assertFalse(m2.equals(m1));
    assertNotSame(m1.hashCode(), m2.hashCode());
    assertNotSame(m1, m3);
    assertFalse(m3.equals(m1));
    assertFalse(m1.equals(m3));
    assertNotSame(m1.hashCode(), m3.hashCode());
    assertNotSame(m1, m4);
    assertFalse(m4.equals(m1));
    assertFalse(m1.equals(m4));
    assertNotSame(m1.hashCode(), m4.hashCode());
    assertFalse(m1.equals(new Object()));
    assertTrue(m1.equals(new MleMarker(MLEMARKER_NAME, true, 0, new GuidGenerator().create(new Object()))));
    assertEquals(m1.hashCode(), new MleMarker(MLEMARKER_NAME, true, 0, new GuidGenerator().create(new Object())).hashCode());
  }
}
