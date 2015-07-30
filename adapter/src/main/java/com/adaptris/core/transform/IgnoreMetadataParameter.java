package com.adaptris.core.transform;

import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XmlTransformParameter} implementation that returns a null map.
 * 
 * @author lchan
 * @config xml-transform-no-parameters
 */
@XStreamAlias("xml-transform-no-parameters")
public class IgnoreMetadataParameter implements XmlTransformParameter {


  public IgnoreMetadataParameter() {
  }

  @Override
  public Map createParameters(AdaptrisMessage msg, Map existingParams) {
    return null;
  }
}
