package com.adaptris.core.transform;

import com.adaptris.transform.ProcessorHandle;
import com.adaptris.transform.TransformFramework;
import com.adaptris.transform.ff.FfTransform;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Flat file transformation service.
 * </p>
 * 
 * @config flat-file-transform-service
 * 
 * @license BASIC
 * @author sellidge
 */
@XStreamAlias("flat-file-transform-service")
public class FfTransformService extends TransformService {

  /**
   * <p>
   * Returns a new singleton <code>FfTransform</code> framework.
   * </p>
   * @see com.adaptris.core.transform.TransformService#createFramework()
   */
  @Override
  protected TransformFramework createFramework() throws Exception {
    return new FfTransform(new ProcessorHandle());
  }
}
