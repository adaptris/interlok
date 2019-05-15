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

package com.adaptris.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.util.SecurityUtil;

/** Factory for creating output specific security services.
 * 
 * @see SecurityService
 * @see Output
 * @author $Author: lchan $
 */
public abstract class SecurityServiceFactory {

  protected transient static final Logger log = LoggerFactory.getLogger(SecurityServiceFactory.class.getName());
  
  private static SecurityServiceFactory singleton = new SecurityFactory();

  /**
   * @see Object#Object()
   * 
   *  
   */
  public SecurityServiceFactory() {
  }

  /**
   * Get the default instance of the factory.
   * 
   * @return the specified security service factory
   * @see SecurityService
   */
  public static SecurityServiceFactory defaultInstance() {
    return singleton;
  }

  /**
   * Create an instance of the specified security service.
   * 
   * @return the instance as specified by the factory implementation.
   * @throws AdaptrisSecurityException
   *           wrapping any underlying exception
   */
  public abstract SecurityService createService()
      throws AdaptrisSecurityException;

  private static final class SecurityFactory extends SecurityServiceFactory {
    private SecurityFactory() {
      super();
      SecurityUtil.addProvider();
    }

    /**
     * @see com.adaptris.security.SecurityServiceFactory#createService()
     */
    @Override
    public SecurityService createService() throws AdaptrisSecurityException {
      return new StdSecurityService();
    }

  }
}
