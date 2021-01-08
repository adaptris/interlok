package com.adaptris.core.jms;

import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;

public interface TestJmsBroker {
  
  public void perTestSetup() throws Exception;
  
  public JmsConnection getJmsConnection();
  
  public JmsConnection getJmsConnection(BasicActiveMqImplementation vendorImp, boolean useTcp);
  
  public void start() throws Exception;
  
  public void destroy();
  
  public String getName();

}
