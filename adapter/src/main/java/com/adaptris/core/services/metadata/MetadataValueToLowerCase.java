/*
 * $RCSfile: MetadataValueToLowerCase.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/11/29 10:26:56 $
 * $Author: lchan $
 */
package com.adaptris.core.services.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Takes a metadata value and converts it to lower case.
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be changed to lowercase
 * </p>
 * 
 * @config metadata-value-to-lower-case
 * 
 * @license BASIC
 */
@XStreamAlias("metadata-value-to-lower-case")
public class MetadataValueToLowerCase extends ReformatMetadata {

  public MetadataValueToLowerCase() {
    super();
  }

  @Override
  protected String reformat(String toChange, String msgCharset) {
    return toChange.toLowerCase();
  }

}
