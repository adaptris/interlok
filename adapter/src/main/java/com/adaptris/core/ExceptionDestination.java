package com.adaptris.core;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
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
public class ExceptionDestination implements ProduceDestination {

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

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("[");
    result.append(this.getClass().getName());
    result.append("] mappings [");
    result.append(exceptionMapping);
    result.append("] default destination [");
    result.append(defaultDestination);
    result.append("]");

    return result.toString();
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
    if (mapping != null) {
      exceptionMapping = mapping;
    }
    else {
      throw new IllegalArgumentException("Null Exception Mapping");
    }
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
    if ("".equals(s) || s == null) {
      throw new IllegalArgumentException("Null Default Destination");

    }
    defaultDestination = s;
  }
}
