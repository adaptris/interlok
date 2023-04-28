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

package com.adaptris.core.transform;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.XmlHelper;

@SuppressWarnings("deprecation")
public class TransformParameterCase extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  protected static final String XML_DOC = "<document>data</document>";
  protected static final String KEY_STRING_METADATA = "myStringMetadata";
  protected static final String KEY_STRING_METADATA_2 = "anotherStringMetadata";
  protected static final String KEY_OBJECT_METADATA = "myObjectMetadata";
  protected static final String KEY_OBJECT_METADATA_2 = "anotherObjectMetadata";

  protected static final String METADATA_VALUE = "myStringMetadataValue";

  @Test
  public void testIgnoreMetadataParameter() throws Exception {
    IgnoreMetadataParameter p = new IgnoreMetadataParameter();
    Map existingParams = new HashMap();
    AdaptrisMessage msg = createMessage();
    assertNull(p.createParameters(msg, existingParams));
  }

  protected AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_STRING_METADATA, METADATA_VALUE);
    msg.addMetadata(KEY_STRING_METADATA_2, "another value");
    msg.getObjectHeaders().put(KEY_OBJECT_METADATA, XmlHelper.createDocument(XML_DOC));
    msg.getObjectHeaders().put(KEY_OBJECT_METADATA_2, new Object());
    return msg;
  }
}
