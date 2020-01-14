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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.MimeEncoder;

public class AggregatingFtpConsumerTest extends BaseCase {

  public AggregatingFtpConsumerTest() {
  }

  @Test
  public void testDeleteAggregatedFiles() throws Exception {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    assertTrue(consumer.deleteAggregatedFiles());
    assertNull(consumer.getDeleteAggregatedFiles());
    consumer.setDeleteAggregatedFiles(Boolean.FALSE);
    assertNotNull(consumer.getDeleteAggregatedFiles());
    assertFalse(consumer.deleteAggregatedFiles());
  }

  @Test
  public void testFileFilterImp() throws Exception {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    assertEquals(RegexFileFilter.class.getCanonicalName(), consumer.fileFilterImp());
    assertNull(consumer.getFileFilterImp());
    consumer.setFileFilterImp("testFileFilterImp");
    assertNotNull(consumer.getFileFilterImp());
    assertEquals("testFileFilterImp", consumer.fileFilterImp());
  }

  @Test
  public void testEncoder() throws Exception {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    assertNull(consumer.getEncoder());
    consumer.setEncoder(new MimeEncoder());
    assertNotNull(consumer.getEncoder());
    assertEquals(MimeEncoder.class, consumer.getEncoder().getClass());
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
}
