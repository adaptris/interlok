/*
 * $Id: ClientSession.java,v 1.10 2007/09/06 07:59:08 lchan Exp $
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
  /** 
   *  @see com.adaptris.http.HttpSessionImp#initialise()
   */
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

  /** Read the data returned by the remote server
   *  <p>When using HTTP/1.0 you cannot get 1xx responses from the server
   *  because it explicitly states in RFC1945
   *  <code><pre>
   *  9.1  Informational 1xx
   *
   *  This class of status code indicates a provisional response,
   *  consisting only of the Status-Line and optional headers, and is
   *  terminated by an empty line. HTTP/1.0 does not define any 1xx status
   *  codes and they are not a valid response to a HTTP/1.0 request.
   *  However, they may be useful for experimental applications which are
   *  outside the scope of this specification.
   *  </pre></code>
   *  <p>However it is possible to do so when using HTTP/1.1 (from RFC2068)
   *  There are two possible information responses for HTTP/1.1, 100, and 101
   *  <code><pre>
   *  10.1.1 100 Continue
   *
   *  The client may continue with its request. This interim response is
   *  used to inform the client that the initial part of the request has
   *  been received and has not yet been rejected by the server. The client
   *  SHOULD continue by sending the remainder of the request or, if the
   *  request has already been completed, ignore this response. The server
   *  MUST send a final response after the request has been completed.
   *  10.1.2 101 Switching Protocols
   *
   *  The server understands and is willing to comply with the client's
   *  request, via the Upgrade message header field (section 14.41), for a
   *  change in the application protocol being used on this connection. The
   *  server will switch protocols to those defined by the response's
   *  Upgrade header field immediately after the empty line which
   *  terminates the 101 response.
   *
   *  The protocol should only be switched when it is advantageous to do
   *  so.  For example, switching to a newer version of HTTP is
   *  advantageous over older versions, and switching to a real-time,
   *  synchronous protocol may be advantageous when delivering resources
   *  that use such features.
   *  </pre></code>
   *  <p>If a 100 CONTINUE response is received, then we stick around and
   *  wait for another response.  If 101 is received, an IOException is
   *  thrown.
   */
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
        case 100 : // 100 CONTINUE
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