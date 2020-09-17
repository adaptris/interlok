/*
 * Copyright 2018 Adaptris Ltd.
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

package com.adaptris.core.services.exception;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;

public class ExceptionAsStringReportTest extends ExceptionServiceExample {



  @Test
  public void testDoService_Payload() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("This is the exception"));
    ExceptionReportService service = new ExceptionReportService(new ExceptionAsString());
    execute(service, msg);
    assertTrue(msg.getContent().contains("This is the exception"));
  }

  @Test
  public void testDoService_Payload_Exception() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("This is the exception"));
    ExceptionReportService service = new ExceptionReportService(
        new ExceptionAsString().withTarget(new DataOutputParameter<String>() {
          @Override
          public void insert(String data, InterlokMessage msg) throws InterlokException {
            throw new CoreException();
          }
        }));
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testDoService_Metadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("This is the exception"));
    ExceptionReportService service = new ExceptionReportService(
        new ExceptionAsString().withTarget(new MetadataDataOutputParameter("exceptionKey")).withIncludeStackTrace(false));
    execute(service, msg);
    assertTrue(msg.getMetadataValue("exceptionKey").contains("This is the exception"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ExceptionReportService(new ExceptionAsString());
  }

  @Override
  protected String createBaseFileName(Object o) {
    return super.createBaseFileName(o) + "-" + ((ExceptionReportService) o).getExceptionSerializer().getClass().getSimpleName();
  }
}
