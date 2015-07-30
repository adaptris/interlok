package com.adaptris.core.management.properties;


/**
 * Interface for decoding properties.
 *
 * @author gcsiki
 *
 */
public interface Decoder {

  /**
   * Decode the value.
   *
   * @param value the original value
   * @return the decoded value
   * @throws Exception upon exception.
   */
	public String decode(String value) throws Exception;

}
