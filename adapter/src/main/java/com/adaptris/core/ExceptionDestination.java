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

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairCollection;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link ProduceDestination} which matches the Exception class in object metadata to generate a destination
 * string.
 * </p>
 * 
 * @config exception-destination
 */
@XStreamAlias("exception-destination")
@DisplayOrder(order = {"exceptionMapping", "defaultDestination"})
public class ExceptionDestination implements MessageDrivenDestination {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  @NotNull
  @AutoPopulated
  private KeyValuePairCollection exceptionMapping;
  private String defaultDestination;

  public ExceptionDestination() {
    setExceptionMapping(new KeyValuePairCollection());
    setDefaultDestination("Exception");
  }

  public ExceptionDestination(String defaultDestination, KeyValuePairCollection mappings) {
    setExceptionMapping(mappings);
    setDefaultDestination(defaultDestination);
  }

  @Override
  public String getDestination(AdaptrisMessage msg) throws CoreException {
    String destinationName = defaultDestination;
    if (msg.getObjectHeaders().containsKey(OBJ_METADATA_EXCEPTION)) {
      Exception e = (Exception) msg.getObjectHeaders().get(OBJ_METADATA_EXCEPTION);
      Throwable exc = e;
      do {
        if (exceptionMapping.contains(new KeyValuePair(exc.getClass().getName(), ""))) {
          destinationName = exceptionMapping.getValue(exc.getClass().getName());
          break;
        }
      }
      while ((exc = exc.getCause()) != null);
    }
    else {
      log.debug("No Exception in object metadata, using default destination");
    }
    log.trace("Destination found to be " + destinationName);
    return destinationName;
  }

  public KeyValuePairCollection getExceptionMapping() {
    return exceptionMapping;
  }

  /**
   * Set the mapping for exception and destinations.
   * <p>
   * <ul>
   * <li>The key part is the classname of the exception</li>
   * <li>The value part is the destination that will be used</li>
   * </ul>
   * </p>
   *
   * @param mapping the mapping.
   */
  public void setExceptionMapping(KeyValuePairCollection mapping) {
    exceptionMapping = Args.notNull(mapping, "exceptionMappings");
  }

  public String getDefaultDestination() {
    return defaultDestination;
  }

  /**
   * Set the default destination when no exceptions match.
   *
   * @param s the default destination.
   */
  public void setDefaultDestination(String s) {
    defaultDestination = Args.notBlank(s, "defaultDestination");
  }
}
