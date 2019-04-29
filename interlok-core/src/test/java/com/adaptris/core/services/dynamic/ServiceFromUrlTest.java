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
package com.adaptris.core.services.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.LifecycleHelper;

public class ServiceFromUrlTest {

  @Test
  public void testGetInputStream() throws Exception {
    File f = createService();
    String urlStyle = f.getCanonicalPath().replaceAll("\\\\", "/");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("location", urlStyle);
    ServiceFromUrl serviceExtractor = new ServiceFromUrl("file:///%message{location}");
    try {
      LifecycleHelper.initAndStart(serviceExtractor);
      try (InputStream in = serviceExtractor.getInputStream(msg)) {
        assertNotNull(in);
        assertEquals(ServiceList.class,
            DefaultMarshaller.getDefaultMarshaller().unmarshal(in).getClass());
      }
    } finally {
      LifecycleHelper.stopAndClose(serviceExtractor);
    }
  }


  private File createService() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    String xml = DynamicServiceExecutorTest
        .createMessage(new ServiceList(new Service[] {new LogMessageService()})).getContent();
    try (FileWriter w = new FileWriter(f, false)) {
      w.write(xml);
    }
    return f;
  }

}
