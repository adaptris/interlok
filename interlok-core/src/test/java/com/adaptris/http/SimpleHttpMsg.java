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

package com.adaptris.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adaptris.util.stream.StreamUtil;

final class SimpleHttpMsg extends MessageImp {

  private HttpSession owner;

  SimpleHttpMsg() {
    super();
  }

  public void registerOwner(HttpSession s) {
    owner = s;
  }

  /**
   * @see HttpMessage#load(InputStream)
   */
  public void load(InputStream in) throws HttpException {
    try {
      getHeaders().load(in);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      if (getHeaders().getContentLength() < 0) {
        if (owner != null) {
          if (owner.allowsNoContentLength()) {
            StreamUtil.copyStream(in, out);
            getHeaders().put(Http.CONTENT_LENGTH, String.valueOf(out.size()));
          }
        }
      }
      else {
        StreamUtil.copyStream(in, out, getHeaders().getContentLength());
      }
      input = new ByteArrayInputStream(out.toByteArray());
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
  }

  /**
   * @see HttpMessage#writeTo(OutputStream)
   */
  public void writeTo(OutputStream out) throws HttpException {
    HttpHeaders hdr = getHeaders();
    if (!hdr.containsHeader(Http.CONTENT_LENGTH)) {
      hdr.put(Http.CONTENT_LENGTH, String.valueOf(output.size()));
    }
    logMessageInfo();
    hdr.writeTo(out);
    try {
      output.writeTo(out);
      out.flush();
    }
    catch (Exception e) {
      throw new HttpException(e);
    }
  }

}
