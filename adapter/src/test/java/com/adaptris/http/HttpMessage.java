package com.adaptris.http;

import javax.activation.DataSource;

/** A HTTP message.
 *  <p>A Http message unit is considerd to contain 2 parts, the header and the 
 *  actual data.  
 *  This interface merely needs to define the header part, the data can be 
 *  retrieved using the various DataSource methods available.
 *  </p>
 */
public interface HttpMessage extends DataSource, DataTransfer {

  /** Get the headers associated with this message 
   *  @return a set of HttpHeaders
   */
  HttpHeaders getHeaders();
  
  /** Set the headers associate with this message.
   *  @param header the headers.
   */
  void setHeaders(HttpHeaders header);
  
  /** Set the owner of this HttpMessage
   * 
   * @param s the session that owns this message.
   */
  void registerOwner(HttpSession s);
  
}