/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.TruncateMetadata;

public class MetadataServiceImplTest extends MetadataServiceImpl {


  @Test
  public void testMetadataLogger() {
    TruncateMetadata logger = new TruncateMetadata(10);
    withMetadataLogger(logger);
    assertNotNull(getMetadataLogger());
    assertEquals(logger, getMetadataLogger());
    assertEquals(TruncateMetadata.class, getMetadataLogger().getClass());
  }

  @Test
  public void testLogMetadata_TRACE() {
    logMetadata(TRACE, "{}", array());
    logMetadata(TRACE, "{}", list());
  }

  @Test
  public void testLogMetadata_DEBUG() {
    logMetadata(DEBUG, "{}", array());
    logMetadata(DEBUG, "{}", list());
  }

  @Test
  public void testLogMetadata_INFO() {
    logMetadata(INFO, "{}", array());
    logMetadata(INFO, "{}", list());
  }

  @Test
  public void testLogMetadata_WARN() {
    logMetadata(WARN, "{}", array());
    logMetadata(WARN, "{}", list());
  }

  @Test
  public void testLogMetadata_ERROR() {
    logMetadata(ERROR, "{}", array());
    logMetadata(ERROR, "{}", list());
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    // dummy method
  }

  private static MetadataElement[] array() {
    return new MetadataElement[] {
        new MetadataElement("key1", "val1"), new MetadataElement("key2", "val2")
    };
  }

  private static MetadataCollection list() {
    return new MetadataCollection(array());
  }

}
