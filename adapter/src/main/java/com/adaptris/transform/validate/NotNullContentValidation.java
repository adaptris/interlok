package com.adaptris.transform.validate;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Check the content and ensure it is null.
 * 
 * @config xml-content-not-null
 * 
 * @author sellidge
 */
@XStreamAlias("xml-content-not-null")
public class NotNullContentValidation implements ContentValidation {

  public boolean isValid(String content) {
    return !isBlank(content);
  }


  public String getMessage() {
    return "Data element cannot be empty";
  }
}