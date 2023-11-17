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

package com.adaptris.core.stubs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.util.stream.StreamUtil;

public class MessageHelper {

  public static AdaptrisMessage createMessage(AdaptrisMessageFactory factory, String filename) throws IOException {
    AdaptrisMessage m = factory.newMessage();
    if (m instanceof FileBackedMessage) {
      ((FileBackedMessage) m).initialiseFrom(new File(filename));
    }
    else {
      StreamUtil.copyAndClose(new FileInputStream(new File(filename)), m.getOutputStream());
    }
    return m;
  }

  public static AdaptrisMessage createMessage(String filename) throws IOException {
    return createMessage(new DefaultMessageFactory(), filename);
  }

  public static MultiPayloadAdaptrisMessage createMultiPayloadMessage(String payloadId, String filename) throws IOException {
    return createMultiPayloadMessage(payloadId, filename, null);
  }

  public static MultiPayloadAdaptrisMessage createMultiPayloadMessage(String payloadId, String filename, String encoding) throws IOException {
    MultiPayloadMessageFactory factory = new MultiPayloadMessageFactory();
    factory.setDefaultPayloadId(payloadId);
    if (encoding != null) {
      factory.setDefaultCharEncoding(encoding);
    }
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)factory.newMessage();
    StreamUtil.copyAndClose(new FileInputStream(new File(filename)), message.getOutputStream(payloadId));
    return message;
  }
}
