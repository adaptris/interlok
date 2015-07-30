package com.adaptris.core.transform;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.xml.transform.Transformer;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.xml.XmlTransformer;

/**
 * Interface for handling parameters passed into an XML transform.
 * 
 * @author lchan
 * 
 */
public interface XmlTransformParameter {


  /**
   * Create a Map that will be passed into {@link XmlTransformer#transform(Transformer , Reader , Writer , String , Map )}
   * 
   * @param msg the {@link AdaptrisMessage} used to build the parameters.
   * @param existingParams any existing parameters that might already be configured, null otherwise.
   * @return the parameters to pass into the transform.
   */
  Map createParameters(AdaptrisMessage msg, Map existingParams) throws ServiceException;
}
