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
package com.adaptris.core.http.client.net;

/**
 * For use with {@link BranchingHttpRequestService}.
 * 
 *
 */
public interface StatusEvaluator {

  /**
   * Do we match the http status
   * 
   * @param httpStatus the HTTP server response code (e.g. 500)
   * @return true/false.
   */
  boolean matches(int httpStatus);

  /**
   * The service ID to use if a match.
   * 
   * @return the service id to use.
   */
  String serviceId();
}
