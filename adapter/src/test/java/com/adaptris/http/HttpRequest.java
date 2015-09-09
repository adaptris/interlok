package com.adaptris.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.stream.UnbufferedLineInputStream;

/** This class encapsulates the Http Request line.
 * From <a href="http://www.faqs.org/rfcs/rfc2616.html">rfc2616</a>
 * <code><pre>
 * 5.1 Request-Line
 *
 * The Request-Line begins with a method token, followed by the
 * Request-URI and the protocol version, and ending with CRLF. The
 * elements are separated by SP characters. No CR or LF is allowed
 * except in the final CRLF sequence.
 *
 *      Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
 *  </pre></code>
 */
public final class HttpRequest implements DataTransfer {
  
  private String method, version, uri;
  private transient Log logR;
  
  /** @see Object#Object()
   * 
   *
   */
  public HttpRequest() {
    method = "GET";
    version = Http.VERSION_1;
    uri = "/";
    logR = LogFactory.getLog(this.getClass());
  }
  /** Set the method.
   * @param s set the method to be associated with this set of HTTP headers
   * e.g. POST GET PUT
   */
  public void setMethod(String s) {
    this.method = s;
  }
  
  /** Set the uri.
   * <p>e.g. /directory/index.html, or
   * /index.jsp?param1=x&param2=y&param3=z</p>
   * @param uri set the uri to be associated with this set of HTTP headers
   *
   */
  public void setURI(String uri) {
    this.uri = uri;
  }
  
  /** Return the http Version
   * @return the http Version associated with this set of HTTP headers
   * generally HTTP/1.0
   */
  public String getVersion() {
    return version;
  }
  
  /** Return the Method associated with this set of headers
   * @return the http method associated with this set of HTTP headers
   */
  public String getMethod() {
    return method;
  }
  
  /** Return the URI.
   * @return the URI associated with this set of HTTP headers
   */
  public String getURI() {
    return uri;
  }
  
  /** Set the Version.
   * @param version set the version to be associated with this set of HTTP 
   * headers
   * generally HTTP/1.0 or HTTP/1.1
   */
  public void setVersion(String version) {
    this.version = version;
  }
  
  /** Write the request line to the supplied outputstream.
   *  @param out the outputstream
   *  @throws HttpException on error.
   *  @see DataTransfer#writeTo(OutputStream)
   */
  public void writeTo(OutputStream out)
  throws HttpException {
    try {
      logR.trace("Writing Request:- " + toString());      
      PrintStream p = new PrintStream(out);
      p.print(toString());
      p.print(Http.CRLF);
      p.flush();
    } catch (Exception e) {
      throw new HttpException(e);
    }
  }
  
  /** Parse an inputstream that contains a request.
   *  @see DataTransfer#load(InputStream)
   */
  public void load(InputStream in) throws HttpException {
    UnbufferedLineInputStream unbuffered = new UnbufferedLineInputStream(in);
    String line;
    try {
      synchronized (in) {
        line = unbuffered.readLine();
        
        StringTokenizer st = new StringTokenizer(line);
        if (line.startsWith("HTTP/")) {
          throw new HttpException("Input is not a HttpRequest");
        } else {
          // It's a request
          setMethod(st.nextToken());
          setURI(st.nextToken());
          if (st.hasMoreTokens()) {
            setVersion(st.nextToken());
          }
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
    sb.append(method);
    sb.append(Http.SPACE);
    sb.append(uri);
    sb.append(Http.SPACE);
    sb.append(version);
    return sb.toString();
  }
  
  /**
   * <p>
   * Returns the file element of the URI, where the file element is the URI up 
   * to but not including any paramters or references.  If no parameters or 
   * references exist the URI is returned.
   * </p>
   * @return the file element of the URI
   */
  public String getFile() {
    String result = this.getURI();
    
    if (uri.indexOf("?") > -1) {
      result = uri.substring(0, uri.indexOf("?"));
    }
    else {
      if (uri.indexOf("#") > -1) {
        result = uri.substring(0, uri.indexOf("#"));
      }
    }
    
    logR.debug("file element of URI [" + result + "]");
    
    return result;
  }
  
  /**
   * <p>
   * Returns a <code>Map</code> of the query parameters.  This implementation 
   * doesn't handle URL encoding (e.g. spaces) due to lack of time.
   * </p>
   * @return a <code>Map</code> of the query parameters
   */
  public Map getParameters() {
    Map result = new HashMap();
    
    if (uri.indexOf("?") > -1) {  // uri contains params
      int start = uri.indexOf("?") + 1;
      int end = uri.indexOf("#");
      
      if (end == -1) {  // uri doesn't contain reference
        end = uri.length();
      }
      
      StringTokenizer params 
        = new StringTokenizer(uri.substring(start, end), "&");
      
      while (params.hasMoreTokens()) {
        String param = params.nextToken();
        
        // at least three chars... 
        if (param.length() > 2) {
          // and contain equals but not first or last char
          if (param.indexOf("=") > -1  
            && param.indexOf("=") != 0  
            && param.indexOf("=") != param.length() - 1) {
            
            String key = param.substring(0, param.indexOf("="));
            String value = param.substring(param.indexOf("=") + 1);
            
            result.put(key, value);
          }
        }
        else { // no equals return empty Map
          result.clear();
          break;
        }
      }
    }
    
    return result;
  }
}
