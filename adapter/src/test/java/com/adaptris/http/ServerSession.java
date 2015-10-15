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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.HexDump;

/** Container for a http server request response session.
 */
class ServerSession extends HttpSessionImp {

  public boolean allowsNoContentLength() {
    return false;
  }

  /**
   *  @see com.adaptris.http.HttpSessionImp#initialise()
   */
  protected void initialise() throws HttpException {
    try {
      responseMessage = httpMessageFactory.create();
      responseMessage.registerOwner(this);
      setResponseDefaults();
      getSocket().setReceiveBufferSize(8192);
      InputStream socketInput = getSocket().getInputStream();
      httpRequestLine.load(socketInput);
      requestMessage = httpMessageFactory.create();
      requestMessage.registerOwner(this);
      requestMessage.load(socketInput);
      displayHttpRequest();
    }
    catch (Exception e) {
      throw new HttpException(e);
    }
  }

  private void setResponseDefaults() {
    HttpHeaders hdr = responseMessage.getHeaders();
    hdr.put(Http.SERVER, "Adaptris HttpServer-Engine $Revision: 1.8 $");
    hdr.put(Http.DATE, (new Date()).toString());
    hdr.put(Http.CONNECTION, Http.CLOSE);
  }

  private void displayHttpRequest() throws IOException {
    HttpHeaders hdr = requestMessage.getHeaders();
    if (logR.isTraceEnabled()) {
      logR.trace("\n" + httpRequestLine + "\n" + hdr.toString());
    }
    if (socketLogger.isTraceEnabled()) {
      // We know that the underlying implementation supports mark
      // for the request message as it's a ByteArrayInputStream, but
      // that's not always the case I suppose.
      InputStream i = requestMessage.getInputStream();
      if (i.markSupported()) {
        i.mark(
          (hdr.getContentLength() <= 0)
            ? Integer.MAX_VALUE
            : hdr.getContentLength());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtil.copyStream(i, out, hdr.getContentLength());
        i.reset();
        socketLogger.trace("Data :\n" + HexDump.parse(out.toByteArray()));
      }
    }
  }
  /** @see HttpSession#commit()
   */
  protected void commitToSocket() throws HttpException {
    try {
      OutputStream socketOutput = getSocket().getOutputStream();
      httpResponseLine.writeTo(socketOutput);
      responseMessage.writeTo(socketOutput);
      socketOutput.flush();
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
    finalizeSocket();
  }

  /**
   *  @see HttpSession#setRequestMessage(com.adaptris.http.HttpMessage)
   */
  public void setRequestMessage(HttpMessage msg) {
    throw new UnsupportedOperationException(
      "A Server session cannot " + "issue a request");
  }

  /**
   *  @see com.adaptris.http.HttpSession#getRequestMessage()
   */
  public HttpMessage getRequestMessage() {
    return requestMessage;
  }

  /**
   *  @see HttpSession#setResponseMessage(com.adaptris.http.HttpMessage)
   */
  public void setResponseMessage(HttpMessage msg) {
    responseMessage = msg;
  }

  /**
   *  @see com.adaptris.http.HttpSession#getResponseMessage()
   */
  public HttpMessage getResponseMessage() {
    return responseMessage;
  }

  /**
   *  @see HttpSession#setRequestLine(com.adaptris.http.HttpRequest)
   */
  public void setRequestLine(HttpRequest req) {
    throw new UnsupportedOperationException(
      "A Server session cannot " + "issue a request");
  }

  /**
   *  @see HttpSession#setResponseLine(com.adaptris.http.HttpResponse)
   */
  public void setResponseLine(HttpResponse resp) {
    httpResponseLine = resp;
  }

}
