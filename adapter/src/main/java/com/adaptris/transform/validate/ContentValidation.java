package com.adaptris.transform.validate;

/** Interface for validating arbitary XML content against the relevant
 *  schema.
 * @author sellidge
 * @author $Author: lchan $
 */
public interface ContentValidation {
  /** Parse the content and check it's validity.
   * 
   * @param content the content to parse.
   * @return true or false
   * @throws Exception on a fatal error that could not be handled.
   */
  boolean isValid(String content) throws Exception;

  /** Get the message that caused <code>false</code> to be returned
   * 
   * @return the message
   */
  String getMessage();
}