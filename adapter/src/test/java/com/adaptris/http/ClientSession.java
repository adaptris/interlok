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

import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.HexDump;

/** Container for a Http Client Session
 */
class ClientSession extends HttpSessionImp {

  public boolean allowsNoContentLength() {
    return true;
  }

  protected void initialise() throws HttpException {
    requestMessage = httpMessageFactory.create();
    requestMessage.registerOwner(this);
    HttpHeaders hdr = requestMessage.getHeaders();
    hdr.put(Http.USERAGENT, "Adaptris HttpClient-Engine $Revision: 1.10 $");
    hdr.put(Http.ACCEPT, Http.DEFAULT_ACCEPT);
    hdr.put(Http.CONNECTION, Http.CLOSE);
    hdr.put(Http.CONTENT_TYPE, "text/plain");

  }

  /** @see HttpSession#commit()
   */
  protected void commitToSocket() throws HttpException {
    try {
      OutputStream socketOut = getSocket().getOutputStream();
      httpRequestLine.writeTo(socketOut);
      requestMessage.writeTo(socketOut);
      socketOut.flush();
      responseMessage = httpMessageFactory.create();
      responseMessage.registerOwner(this);
      readResponse();
    } catch (IOException e) {
      throw new HttpException(e);
    } catch (IllegalStateException e) {
      throw new HttpException(e);
    }
    finalizeSocket();
  }

  private void readResponse()
    throws IOException, IllegalStateException, HttpException {
    InputStream socketInput = getSocket().getInputStream();
    handleResponseHeaders(socketInput);
    displayHttpResponse();
  }

  private void handleResponseHeaders(InputStream in)
    throws IOException, HttpException {
    boolean gotRealResponse = false;
    boolean isv10 = httpRequestLine.getVersion().equalsIgnoreCase("HTTP/1.0");
    while (!gotRealResponse) {
      httpResponseLine.load(in);
      if (isv10 && httpResponseLine.getResponseCode() < 200) {
        // RFC1945 (HTTP 1.0)
        throw (
          new IOException(
            "Informational response 1xx to a " + "HTTP/1.0 Request not valid"));
      }
      switch (httpResponseLine.getResponseCode()) {
      case 102: // 102 CONTINUE
          {
            // do absolutely nothing, so we just go round the loop after read
            // rest of the data.
            break;
          }
        case 101 : // 101 SWITCH PROTOCOLS
          {
            throw (
              new IOException(
                "Received 101 switch protocol response - " + "invalid"));
          }
        default :
          {
            responseMessage.load(in);
            gotRealResponse = true;
            break;
          }
      }
    }
    return;
  }

  /** @see HttpSession#setRequestMessage(HttpMessage)
   */
  public void setRequestMessage(HttpMessage msg) {
    requestMessage = msg;
    requestMessage.registerOwner(this);
  }

  /** @see HttpSession#getRequestMessage()
   */
  public HttpMessage getRequestMessage() {
    return requestMessage;
  }

  /** setResponseMessage is not supported by a client session.
   *  <p>If this method is invoked, an UnsupportedOperationException will be 
   *  thrown
   *  @see HttpSession#setResponseMessage(HttpMessage)
   *  @see UnsupportedOperationException
   *  @param msg the Message
   */
  public void setResponseMessage(HttpMessage msg) {
    throw new UnsupportedOperationException(
      "A Client session cannot " + "issue a response");
  }

  /** @see HttpSession#getResponseMessage()
   */
  public HttpMessage getResponseMessage() {
    return responseMessage;
  }

  /** @see HttpSession#setRequestLine(HttpRequest)
   */
  public void setRequestLine(HttpRequest req) {
    httpRequestLine = req;
  }

  /** setResponseLine is not supported by a client session.
   *  <p>If this method is invoked, an UnsupportedOperationException will be 
   *  thrown
   *  @see HttpSession#setResponseLine(HttpResponse)
   *  @param resp the response
   *  @see UnsupportedOperationException
   */
  public void setResponseLine(HttpResponse resp) {
    throw new UnsupportedOperationException(
      "A Client session cannot " + "issue a response");
  }

  private void displayHttpResponse() throws IOException {
    HttpHeaders hdr = responseMessage.getHeaders();
    if (logR.isTraceEnabled()) {
      logR.trace("\n" + httpResponseLine + "\n" + hdr.toString());
    }
    if (socketLogger.isTraceEnabled()) {
      // We know that the underlying implementation supports mark
      // for the request message as it's a ByteArrayInputStream, but
      // that's not always the case I suppose.
      InputStream i = responseMessage.getInputStream();
      if (i.markSupported()) {
        i.mark(
          (hdr.getContentLength() < 0)
            ? Integer.MAX_VALUE
            : hdr.getContentLength());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (hdr.getContentLength() <= 0) {
          StreamUtil.copyStream(i, out);
        } else {
          StreamUtil.copyStream(i, out, hdr.getContentLength());
        }
        i.reset();
        socketLogger.trace("Data :\n" + HexDump.parse(out.toByteArray()));
      }
    }
  }

}
