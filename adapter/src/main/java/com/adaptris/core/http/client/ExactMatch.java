/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.http.client;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.http.client.net.BranchingHttpRequestService;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link StatusEvaluator} for use with {@link BranchingHttpRequestService}.
 * <p>
 * Matches a service id against an exact http response code. e.g. statusCode=200 to match {@code HTTP OK}. It's an exact match; you
 * probably don't want to use it, as you'll have to specify one for {@code 202 Accepted}, one for {@code 201 Created} etc. even
 * though they're all would logically point to the same success ervice id.
 * </p>
 * 
 * @config http-status-exact-match
 *
 */
@XStreamAlias("http-status-exact-match")
@DisplayOrder(order = {"serviceId", "statusCode"})
public class ExactMatch extends StatusEvaluatorImpl {

  private int statusCode;

  public ExactMatch() {
    super();
  }

  public ExactMatch(int code, String serviceId) {
    this();
    setServiceId(serviceId);
    setStatusCode(code);
  }

  @Override
  public boolean matches(int responseCode) {
    return this.statusCode == responseCode;
  }

  /**
   * @return the responseCode
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * @param s the responseCode to set
   */
  public void setStatusCode(int s) {
    this.statusCode = s;
  }

}
