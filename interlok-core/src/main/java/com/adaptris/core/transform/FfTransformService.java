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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.transform.TransformFramework;
import com.adaptris.transform.ff.FfTransform;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Flat file transformation service.
 * </p>
 * 
 * @config flat-file-transform-service
 * 
 * 
 * @author sellidge
 */
@XStreamAlias("flat-file-transform-service")
@AdapterComponent
@ComponentProfile(summary = "Transform from Flat-File to XML", tag = "service,transform,xml")
@DisplayOrder(order = {"url", "outputMessageEncoding", "cacheTransforms", "allowOverride", "metadataKey"})
public class FfTransformService extends TransformService {
  /**
   * <p>
   * Returns a new singleton <code>FfTransform</code> framework.
   * </p>
   * @see com.adaptris.core.transform.TransformService#createFramework()
   */
  @Override
  protected TransformFramework createFramework() throws Exception {
    return new FfTransform();
  }
}
