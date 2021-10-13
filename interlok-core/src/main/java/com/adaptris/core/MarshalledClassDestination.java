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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.adaptris.validation.constraints.ConfigDeprecated;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of <code>ProduceDestination</code>.
 * <p>For a given AdaptrisMessage object that is a marshalled class, this
 * destination will unmarshall the class, check against the <code>
 * KeyValuePairSet</code> that has been configured and return the value
 * associated with the classname.
* <p>
 * In the adapter configuration file this class is aliased as <b>marshalled-class-destination</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>

 * @author lchan / $Author: lchan $
 */
@XStreamAlias("marshalled-class-destination")
@DisplayOrder(order = {"defaultDestination", "mappings", "marshaller"})
@Deprecated(forRemoval = true)
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public final class MarshalledClassDestination implements MessageDrivenDestination {

  @NotNull
  @AutoPopulated
  private KeyValuePairSet mappings;
  @AdvancedConfig(rare = true)
  private AdaptrisMarshaller marshaller;
  @NotBlank
  private String defaultDestination;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());

  /** @see Object#Object()
   */
  public MarshalledClassDestination() {
    mappings = new KeyValuePairSet();
  }

  private AdaptrisMarshaller marshallerToUse() {
    return DefaultMarshaller.defaultIfNull(getMarshaller());
  }

  /** Get the destination based on the type of object this AdaptrisMessage is.
   *  <p>The following rules will apply for the purposes of resolving a
   *  destination map.
   *  <ul>
   *  <li>If the message is not a marshalled object, then the default
   *  destination is returned.</li>
   *  <li>If no mapping can be found for the object, then the default
   *  destination is returned.</li>
   *  <li>If the default destination is not set, and the above rules apply,
   *  then a RuntimeException will be thrown.</li>
   *  </ul>
   * @see ProduceDestination#getDestination(AdaptrisMessage)
   */
  @Override
  public String getDestination(AdaptrisMessage msg) {
    String destination = null;
    Object msgObject = null;
    try {
      msgObject = marshallerToUse().unmarshal(msg.getContent());
      String className = msgObject.getClass().getName();
      logR.trace("Unmarshalled [" + className + "]");
      if (mappings.contains(new KeyValuePair(className, ""))) {

        destination = mappings.getValue(className);
      }
      else {
        logR.trace("[" + className + "] not found, using default");
        destination = defaultDestination;
      }
    }
    catch (Exception e) {
      logR.trace("Payload could not be unmarshalled using default destination");
      destination = defaultDestination;
    }

    logR.trace("Destination for message [" + destination +"]");
    if (StringUtils.isEmpty(destination)) {
      throw new RuntimeException("Could not resolve destination");
    }
    return destination;
  }

  /** Set the mappings.
   *
   * @param set the set of mappings
   */
  public void setClassMappings(KeyValuePairSet set) {
    mappings = set;
  }

  /** Get the mappings.
   *
   * @return the set of mappings
   */
  public KeyValuePairSet getClassMappings() {
    return mappings;
  }

  /** Get the marshaller used to  unmarshall the AdaptrisMessage object.
   *
   * @return the marshaller
   * @see AdaptrisMarshaller
   */
  public AdaptrisMarshaller getMarshaller() {
    return marshaller;
  }

  /**
   * Set the marshaller to be used to unmarshaller the AdaptrisMessage object.
   * 
   * @param m the marshaller (default is {@link DefaultMarshaller#getDefaultMarshaller()})
   * @see AdaptrisMarshaller
   */
  public void setMarshaller(AdaptrisMarshaller m) {
    marshaller = m;
  }

  /** Set the default destination to be used if no mapping could be found.
   *
   * @param d the default destination.
   */
  public void setDefaultDestination(String d) {
    defaultDestination = d;
  }

  /** Get the default destination.
   *
   * @return the default destination.
   */
  public String getDefaultDestination() {
    return defaultDestination;
  }

}
