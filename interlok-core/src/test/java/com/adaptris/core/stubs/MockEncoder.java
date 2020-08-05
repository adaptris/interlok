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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoderImp;
import com.adaptris.core.CoreException;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;

public class MockEncoder extends AdaptrisMessageEncoderImp<OutputStream, InputStream> {

  public void writeMessage(AdaptrisMessage msg, OutputStream target) throws CoreException {

    try {
      target.write(msg.getPayload());
      target.flush();
    }
    catch (Exception e) {
      throw new CoreException("Could not encode the AdaptrisMessage object", e);
    }
  }

  /**
   * Decode into an <code>AdaptrisMessage</code> object.
   * <p>
   * The source object is assumed to be of the type <code>InputStream</code>
   * </p>
   *
   * @see com.adaptris.core.AdaptrisMessageEncoder#readMessage(java.lang.Object)
   */
  public AdaptrisMessage readMessage(InputStream source) throws CoreException {
    AdaptrisMessage msg = null;
    try {
      msg = currentMessageFactory().newMessage();
      try (OutputStream out = msg.getOutputStream()) {
        IOUtils.copy(source, out);
      }
    }
    catch (Exception e) {
      throw new CoreException("Could not parse supplied bytes into an AdaptrisMessage object", e);
    }
    return msg;
  }
}
