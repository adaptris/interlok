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

package com.adaptris.core.transform;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.xml.transform.Transformer;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.xml.XmlTransformer;

/**
 * Interface for handling parameters passed into an XML transform.
 * 
 * @author lchan
 * 
 */
public interface XmlTransformParameter {


  /**
   * Create a Map that will be passed into {@link XmlTransformer#transform(Transformer , Reader , Writer , String , Map )}
   * 
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} used to build the parameters.
   * @param existingParams any existing parameters that might already be configured, null otherwise.
   * @return the parameters to pass into the transform.
   */
  Map createParameters(AdaptrisMessage msg, Map existingParams) throws ServiceException;
}
