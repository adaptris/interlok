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

package com.adaptris.core.services.metadata;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Iterator;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * <code>Service</code> which sets the ID of the <code>Service</code> to apply next based on whether any value is present against
 * any configured metadata key.
 * </p>
 * 
 * @config metadata-exists-branching-service
 * 
 */
@XStreamAlias("metadata-exists-branching-service")
public class MetadataExistsBranchingService 
  extends MetadataBranchingServiceImp {
  
  @NotBlank
  private String metadataExistsServiceId;

  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.setNextServiceId(this.getDefaultServiceId());    
    Iterator itr = this.getMetadataKeys().iterator();
    
    while (itr.hasNext()) {
      String key = (String) itr.next();
      String value = msg.getMetadataValue(key);
      
      if (!isEmpty(value)) {
        msg.setNextServiceId(this.getMetadataExistsServiceId());
        break;
      }
    }
    
    // logging is in BranchingServiceCollection
  }
  
  /**
   * <p>
   * Returns the <code>ServiceId</code> to use next if metadata (other than
   * "") exists against any of the configured keys. 
   * </p>
   * @return the <code>ServiceId</code> to use next if metadata (other than
   * "") exists against any of the configured keys
   */
  public String getMetadataExistsServiceId() {
    return metadataExistsServiceId;
  }
  
  /**
   * <p>
   * Sets the <code>ServiceId</code> to use next if metadata (other than
   * "") exists against any of the configured keys.
   * </p>
   * @param s the <code>ServiceId</code> to use next if 
   * metadata (other than "") exists against any of the configured keys
   */
  public void setMetadataExistsServiceId(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null param");
    }
    this.metadataExistsServiceId = s;
  }
  
  /** @see java.lang.Object#toString() */
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    
    result.append(" metadata exists Service ID [");
    result.append(this.getMetadataExistsServiceId());
    result.append("]");
    
    return result.toString();
  }
}
