package com.adaptris.core.runtime;

import java.net.URL;

import com.adaptris.core.CoreException;

/**
 * <p>
 * Implementations of this interface will perform actions on the xml configuration before
 * the configuration is unmarshalled.
 * </p>
 * @author amcgrath
 *
 */
public interface ConfigurationPreProcessor {
  
  public String process(String xml) throws CoreException;
  
  public String process(URL urlToXml) throws CoreException;

}
