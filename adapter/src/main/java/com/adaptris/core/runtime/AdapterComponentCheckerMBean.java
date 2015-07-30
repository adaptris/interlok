package com.adaptris.core.runtime;

import com.adaptris.core.CoreException;
import com.adaptris.core.SerializableAdaptrisMessage;

/**
 * MBean for the UI to ask the adapter to test configuration.
 * 
 * @author lchan
 * 
 */
public interface AdapterComponentCheckerMBean extends BaseComponentMBean {

  String COMPONENT_CHECKER_TYPE = AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=ComponentChecker";


  /**
   * Check that this XML will initialise.
   * 
   * @param xml the XML configuration, which could be a service, or a connection.
   * @throws CoreException wrapping any other exception.
   */
  void checkInitialise(String xml) throws CoreException;

  /**
   * Apply the configured services to the msg.
   * 
   * @param xml String XML representation of the service (or service-list)
   * @param msg the message.
   * @return the result of applying these services.
   * @throws CoreException wrapping any other exception
   */
  SerializableAdaptrisMessage applyService(String xml, SerializableAdaptrisMessage msg) throws CoreException;

}
