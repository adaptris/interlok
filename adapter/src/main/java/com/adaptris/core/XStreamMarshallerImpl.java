package com.adaptris.core;

import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

/**
 * Abstract XStream version of {@link AdaptrisMarshaller}
 *
 * @author D Sefton
 */
public abstract class XStreamMarshallerImpl extends AbstractMarshaller{

  protected transient XStream instance;

  /**
   * Typically it will do something like this:-
   * 
   * <pre>
   * {@code 
   * if (instance == null){
   *    create and configure a new instance
   * }
   * return instance;
   * }
   * </pre>
   * 
   * @return a pre-configured instance.
   * 
   */
  protected abstract XStream getInstance();

  @Override
  public String marshal(Object obj) throws CoreException {
    String xmlResult = getInstance().toXML(obj);
    return xmlResult;
  }

  @Override
  public void marshal(Object obj, Writer writer) throws CoreException {
    try {
      getInstance().toXML(obj, writer);
      writer.flush();
    }
    catch (Exception ex) {
      throw new CoreException(ex);
    }
  }

  @Override
  public Object unmarshal(Reader reader) throws CoreException {
    Object result = null;
    try {
      result = getInstance().fromXML(reader);
      reader.close();

    }
    catch (Exception e) {
      throw new CoreException(e);
    }

    return result;
  }
}
