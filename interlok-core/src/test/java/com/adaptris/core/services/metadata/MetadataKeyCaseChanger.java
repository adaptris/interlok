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

package com.adaptris.core.services.metadata;

import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.metadata.RegexMetadataFilter;

public abstract class MetadataKeyCaseChanger extends MetadataServiceExample {


  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    addMetadata(msg);
    return msg;
  }

  @Test
  public void testService() throws Exception {
    ReformatMetadataKey service = createService();
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    doAssertions(msg);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ReformatMetadataKey service = createService();
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern("^.*MetadataKeyRegularExpression.*");
    filter.addIncludePattern("^.*AnotherPattern.*");
    filter.addExcludePattern("^.*SomeExclusions.*");
    service.setKeysToModify(filter);
    return service;
  }

  abstract ReformatMetadataKey createService();

  abstract void doAssertions(AdaptrisMessage msg);

  abstract void addMetadata(AdaptrisMessage msg);


}
