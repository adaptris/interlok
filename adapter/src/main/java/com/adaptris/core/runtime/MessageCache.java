package com.adaptris.core.runtime;

public interface MessageCache {

  public void put(CacheableAdaptrisMessageWrapper message);
  
  public CacheableAdaptrisMessageWrapper remove(String messageId);
  
  public boolean contains(String messageId);
  
  public void init();
  
  public void start();
  
  public void stop();
  
  public void close();
  
}
