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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.stream.UnbufferedLineInputStream;

/** This class encapsulates the Http Response Status line.
 * From <a href="http://www.faqs.org/rfcs/rfc2616.html">rfc2616</a>
 * <code><pre>
 * 6.1 Status-Line
 *
 * The first line of a Response message is the Status-Line, consisting
 * of the protocol version followed by a numeric status code and its
 * associated textual phrase, with each element separated by SP
 * characters. No CR or LF is allowed except in the final CRLF sequence.
 *
 * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
 *  </pre></code>
 */
public final class HttpResponse implements DataTransfer {

  private String responseMessage;
  private String version = Http.VERSION_1;
  private int responseCode;

  private transient Log logR;

  /** @see Object#Object()
   * 
   *
   */
  public HttpResponse() {
    logR = LogFactory.getLog(this.getClass());
  }

  /** get the response.
   * @return the Response code associated with the http headers
   */
  public int getResponseCode() {
    return responseCode;
  }

  /** Get the response message
   * @return the response Message
   */
  public String getResponseMessage() {
    return responseMessage;
  }

  /** Set the response code associated with this HTTP response
   *  @param code the code to be returned back to the client
   */
  public void setResponseCode(String code) {
    if (code == null) throw new IllegalStateException("No Code found " + code);
    responseCode = Integer.parseInt(code.trim());
  }

  /** Set the response code associated with this HTTP response
   *  @param code the code to be returned back to the client
   */
  public void setResponseCode(int code) {
    responseCode = code;
  }

  /** Set the response message associated with this HTTP response
   *  @param message the message to be returned back to the client
   */
  public void setResponseMessage(String message) {
    responseMessage = message;
  }

  /** Set the Version.
   * @param version set the version to be associated with this set of HTTP 
   * headers generally HTTP/1.0 or HTTP/1.1
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /** Return the http Version
   * @return the http Version associated with this set of HTTP headers
   * generally HTTP/1.0
   */
  public String getVersion() {
    return version;
  }

  /** Write the request line to the supplied outputstream.
   *  @param out the outputstream
   *  @throws HttpException on error.
   *  @see DataTransfer#writeTo(OutputStream)
   */
  public void writeTo(OutputStream out) throws HttpException {
    try {
      logR.trace("Writing Response:- " + toString());      

      PrintStream p = new PrintStream(out);
      p.print(toString());
      p.print(Http.CRLF);
      p.flush();
    } catch (Exception e) {
      throw new HttpException(e);
    }
  }

  /** Parse an inputstream that contains a response.
   *  @see DataTransfer#load(InputStream)
   */
  public void load(InputStream in) throws HttpException {
    UnbufferedLineInputStream unbuffered = new UnbufferedLineInputStream(in);
    String line;
    try {
      synchronized (in) {
        line = unbuffered.readLine();

        StringTokenizer st = new StringTokenizer(line);
        if (!line.startsWith("HTTP/")) {
          throw new HttpException(
            "Input is not a HttpResponse - [" + line + "]");
        } else {
          setVersion(st.nextToken());
          setResponseCode(st.nextToken());
          StringBuffer sb = new StringBuffer();
          
          while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(" ");
          }
          setResponseMessage(sb.toString().trim());
        }
      }
    } catch (IOException e) {
      throw new HttpException(e);
    }
    return;
  }

  /** @see Object#toString()
   */
  public String toString() {

    StringBuffer sb = new StringBuffer();
    sb.append(version);
    sb.append(Http.SPACE);
    sb.append(String.valueOf(responseCode));
    sb.append(Http.SPACE);
    sb.append(responseMessage);
    return sb.toString();
  }
}
