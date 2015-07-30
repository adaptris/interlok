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
