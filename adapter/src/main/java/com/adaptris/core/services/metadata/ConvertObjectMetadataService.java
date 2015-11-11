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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} that copies Object metadata to standard metadata, overwriting standard metadata.
 * </p>
 * <p>
 * Object Metadata values are not easily translated to Strings; {@link Object#toString()} is used to perform the string conversion;
 * this may produce metadata values that have no semantic meaning. The key from object metadata is preserved as the metadata key for
 * the new element. Internally, the adapter uses String keys for object metadata; however this is not enforced for custom services,
 * so behaviour may vary depending on what custom components are in use as the key names may not be consistent or predictable.
 * </p>
 * 
 * @config convert-object-metadata-service
 * 
 * 
 */
@XStreamAlias("convert-object-metadata-service")
public class ConvertObjectMetadataService extends ServiceImp {

  @NotBlank
  private String objectMetadataKeyRegexp;

  private transient Pattern objectMetadataKeyPattern;

  public ConvertObjectMetadataService() {
    super();
  }

  public ConvertObjectMetadataService(String regexp) {
    this();
    setObjectMetadataKeyRegexp(regexp);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    Set<MetadataElement> metadataToAdd = new HashSet<MetadataElement>();
    for (Iterator i = msg.getObjectMetadata().entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      String key = entry.getKey().toString();
      if (objectMetadataKeyPattern.matcher(key).matches()) {
        MetadataElement e = new MetadataElement(key, entry.getValue().toString());
        msg.addMetadata(e);
        metadataToAdd.add(e);
      }
    }
    log.trace("metadata added " + metadataToAdd);
  }

  @Override
  protected void initService() throws CoreException {
    objectMetadataKeyPattern = Pattern.compile(getObjectMetadataKeyRegexp());

  }

  @Override
  protected void closeService() {

  }


  public String getObjectMetadataKeyRegexp() {
    return objectMetadataKeyRegexp;
  }

  /**
   * Set the regular expression used to parse object metadata keys.
   * 
   * @param s
   * @see Pattern
   */
  public void setObjectMetadataKeyRegexp(String s) {
    this.objectMetadataKeyRegexp = s;
  }

  @Override
  public void prepare() throws CoreException {
  }

}
