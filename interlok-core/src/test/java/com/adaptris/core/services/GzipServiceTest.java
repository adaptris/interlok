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

package com.adaptris.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.security.MessageDigest;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;

public class GzipServiceTest extends GeneralServiceExample {

  public static final String LINE = "The quick brown fox jumps over the lazy dog";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testZipService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE, "UTF-8");
    execute(new GzipService(), msg);
    assertFalse(MessageDigest.isEqual(LINE.getBytes("UTF-8"), msg.getPayload()));
    execute(new GunzipService(), msg);
    assertEquals("zip then unzip gives same result", LINE, msg
        .getContent());
  }

  @Test
  public void testZipServiceFailure() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(LINE, "UTF-8");
    try {
    execute(new GzipService(), msg);
      fail();
    }
    catch (ServiceException expected) {
      ;
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new GzipService();
  }

}
