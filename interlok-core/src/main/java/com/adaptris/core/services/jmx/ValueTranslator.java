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

package com.adaptris.core.services.jmx;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Implementations of this interface can be used to supply parameters to Jmx operation calls.
 * 
 * @since 3.0.3
 */
public interface ValueTranslator {
  String DEFAULT_PARAMETER_TYPE = "java.lang.String";

  /**
   * Returns the object instance used as a parameter for a JMX operation call.
   * @return the object
   * @throws CoreException 
   */
  public Object getValue(AdaptrisMessage message) throws CoreException;
  
  /**
   * Will set the given object value back into the AdaptrisMessage.
   * @param message
   * @param object
   */
  public void setValue(AdaptrisMessage message, Object object);
  
  /**
   * Returns the fully qualified java class that represents the type of the parameter value.
   * @return fully qualified java class.
   */
  public String getType();
  
  public void setType(String type);
  
}
