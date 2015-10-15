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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoderImp;
import com.adaptris.core.CoreException;

public class MockEncoder extends AdaptrisMessageEncoderImp {

  public void writeMessage(AdaptrisMessage msg, Object target) throws CoreException {

    try {
      if (!(target instanceof OutputStream)) {
        throw new IllegalArgumentException("MockEncoder can only encode to an OutputStream");
      }
      OutputStream encodedOutput = (OutputStream) target;
      encodedOutput.write(msg.getPayload());
      encodedOutput.flush();
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
  public AdaptrisMessage readMessage(Object source) throws CoreException {
    AdaptrisMessage msg = null;
    OutputStream out = null;
    try {
      msg = currentMessageFactory().newMessage();
      if (!(source instanceof InputStream)) {
        throw new IllegalArgumentException("MockEncoder can only decode from an OutputStream");
      }
      out = msg.getOutputStream();
      IOUtils.copy((InputStream) source, out);
    }
    catch (Exception e) {
      throw new CoreException("Could not parse supplied bytes into an AdaptrisMessage object", e);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
    return msg;
  }
}
