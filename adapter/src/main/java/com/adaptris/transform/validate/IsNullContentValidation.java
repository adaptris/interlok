package com.adaptris.transform.validate;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Check the content and ensure it is not null.
 * 
 * @config xml-content-is-null
 * 
 */
@XStreamAlias("xml-content-is-null")
public class IsNullContentValidation implements ContentValidation {

  public boolean isValid(String content) {
    return isBlank(content);
  }

  public String getMessage() {
    return "Data element cannot be empty";
  }
}