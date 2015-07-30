package com.adaptris.core;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XStream version of {@link AdaptrisMarshaller}
 * 
 * @config xstream-marshaller
 * 
 * @author amcgrath
 */
@XStreamAlias("xstream-marshaller")
public class XStreamMarshaller extends XStreamMarshallerImpl {

  public XStreamMarshaller() throws CoreException {
  }
  
  @Override
  protected synchronized XStream getInstance(){
    if (instance == null){
      instance = AdapterXStreamMarshallerFactory.getInstance().createXStream();
    }
    return instance;
  }

}
