package com.adaptris.tester.runtime.helpers;


import com.adaptris.tester.runtime.ServiceTestException;
import com.adaptris.tester.runtime.messages.TestMessage;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

public abstract class Helper implements Closeable {

  @XStreamOmitField
  private Map<String, String> helperProperties;


  public Helper(){
    helperProperties = new HashMap<>();
  }

  public abstract void init() throws ServiceTestException;

  public Map<String, String> getHelperProperties() {
    return helperProperties;
  }

  void addHelperProperty(String key, String value){
    helperProperties.put(key, value);
  }
}
