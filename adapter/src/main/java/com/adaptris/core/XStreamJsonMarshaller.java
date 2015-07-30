package com.adaptris.core;

import com.adaptris.core.AdapterMarshallerFactory.MarshallingOutput;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * JSON implementation of {@link AdaptrisMarshaller} using XStream.
 * 
 * <p>
 * This implementation is not intended to be used for marshalling adapter configuration (although it might be possible), but is
 * intended for use where JSON is the desired data format used for transfer between systems. It uses {@link JettisonMappedXmlDriver}
 * which allows conversion to and from java objects. However, restrictions will apply as detailed in <a
 * href="http://xstream.codehaus.org/json-tutorial.html">the XStream JSON Tutorial</a>
 * </p>
 * 
 * @config xstream-json-marshaller
 */
@XStreamAlias("xstream-json-marshaller")
public class XStreamJsonMarshaller extends XStreamMarshallerImpl {

  public XStreamJsonMarshaller() throws CoreException {
  }

  @Override
  protected synchronized XStream getInstance(){
    if (instance == null){
      instance = AdapterXStreamMarshallerFactory.getInstance().createXStream(MarshallingOutput.JSON);
    }
    return instance;
  }

}
