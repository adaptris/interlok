package com.adaptris.http;

import java.io.IOException;

/** The basic interface for handling an incoming request.
 */
public interface RequestProcessor {

  /** Process in the request in some fashion
   *  @param session the HttpSession.
   *  @throws IOException when a communications error occurs.
   *  @throws IllegalStateException if the state is not correct.
   *  @throws HttpException on other errors.
   */
  void processRequest(HttpSession session)
    throws IOException, IllegalStateException, HttpException;

  /** Get the uri that this request processor is associated with
   *  @return the uri
   */
  String getUri();
}