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

package com.adaptris.core;

import com.adaptris.util.license.License;

/**
 * Interface defining Licensing requirements.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface LicensedComponent {

  /**
   * <p>
   * Verifies that this <code>AdaptrisComponent</code> is enabled based
   * on the current <code>License</code>.  This may be split out to a
   * separate interface if required.
   * </p>
   * @param license the current <code>License</code> object
   * @return true if the license allows this component to be used
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  boolean isEnabled(License license) throws CoreException;
}
