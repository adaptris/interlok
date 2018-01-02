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

package com.adaptris.core.ftp;

import org.apache.commons.io.filefilter.RegexFileFilter;

import com.adaptris.core.BaseCase;
import com.adaptris.core.MimeEncoder;

public class AggregatingFtpConsumerTest extends BaseCase {

  public AggregatingFtpConsumerTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    super.setUp();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testDeleteAggregatedFiles() throws Exception {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    assertTrue(consumer.deleteAggregatedFiles());
    assertNull(consumer.getDeleteAggregatedFiles());
    consumer.setDeleteAggregatedFiles(Boolean.FALSE);
    assertNotNull(consumer.getDeleteAggregatedFiles());
    assertFalse(consumer.deleteAggregatedFiles());
  }

  public void testFileFilterImp() throws Exception {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    assertEquals(RegexFileFilter.class.getCanonicalName(), consumer.fileFilterImp());
    assertNull(consumer.getFileFilterImp());
    consumer.setFileFilterImp(getName());
    assertNotNull(consumer.getFileFilterImp());
    assertEquals(getName(), consumer.fileFilterImp());
  }

  public void testEncoder() throws Exception {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    assertNull(consumer.getEncoder());
    consumer.setEncoder(new MimeEncoder());
    assertNotNull(consumer.getEncoder());
    assertEquals(MimeEncoder.class, consumer.getEncoder().getClass());
  }
}
