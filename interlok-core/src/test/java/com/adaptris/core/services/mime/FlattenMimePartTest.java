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

package com.adaptris.core.services.mime;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.text.mime.BodyPartIterator;

public class FlattenMimePartTest extends MimeServiceExample {


  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected FlattenMimeParts retrieveObjectForSampleConfig() {
    return new FlattenMimeParts();
  }

  public void testFlatten_Nested() throws Exception {
    FlattenMimeParts service = new FlattenMimeParts();
    AdaptrisMessage msg = MimeJunitHelper.createNested();
    execute(service, msg);
    BodyPartIterator mime = MimeHelper.createBodyPartIterator(msg);
    assertEquals(6, mime.size());
  }

  public void testFlatten_NotNested() throws Exception {
    FlattenMimeParts service = new FlattenMimeParts();
    AdaptrisMessage msg = MimeJunitHelper.create();
    execute(service, msg);
    BodyPartIterator mime = MimeHelper.createBodyPartIterator(msg);
    assertEquals(3, mime.size());
  }

  public void testNotMime() throws Exception {
    FlattenMimeParts service = new FlattenMimeParts();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEFG");
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

}
