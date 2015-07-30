package com.adaptris.core.services.metadata;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Trim a metadata values of leading and trailing whitespace.
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be trimmed.
 * </p>
 * 
 * @config trim-metadata-service
 * 
 * @author lchan
 * @license BASIC
 */
@XStreamAlias("trim-metadata-service")
public class TrimMetadataService extends ReformatMetadata {

  public TrimMetadataService() {
    super();
  }

  public TrimMetadataService(String metadataRegexp) {
    super(metadataRegexp);
  }

  @Override
  protected String reformat(String s, String msgCharset) throws Exception {
    return trimToEmpty(s);
  }

}
