package com.adaptris.transform.validate;

import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Match the content against a regular expression.
 * 
 * @config xml-content-regexp
 * 
 */
@XStreamAlias("xml-content-regexp")
public class RegexpContentValidation implements ContentValidation {

  @NotBlank
  @AutoPopulated
  private String pattern;
  private transient Pattern regexpPattern = null;

  public RegexpContentValidation() {
    setPattern(".*");
  }

  public RegexpContentValidation(String pattern) {
    this();
    setPattern(pattern);
  }

  /**
   *  @see ContentValidation#isValid(java.lang.String)
   */
  public boolean isValid(String content) {
    if (regexpPattern == null) {
      regexpPattern = Pattern.compile(getPattern());
    }
    return regexpPattern.matcher(content).matches();
  }

  /**
   *  @see ContentValidation#getMessage()
   */
  public String getMessage() {
    return "Element contents did not validate against the pattern " + pattern;
  }

  /**
   * Get the pattern we are matching against.
   * 
   */
  public final String getPattern() {
    return pattern;
  }

  /**
   * Set the pattern we are matching against..
   * 
   * @param pattern the pattern; default is '.*';
   */
  public final void setPattern(String pattern) {
    this.pattern = pattern;
  }
}