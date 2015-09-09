package com.adaptris.core.services.routing;

import java.util.List;

import com.adaptris.core.ServiceException;

/** Interface used by SyntaxRoutingService.
 *  <p>The contract for this interface is such that
 *  <code>isThisSyntax(String)</code> should only return true, if and only
 *  if <b>ALL</b> the configured patterns are matched within the document.
 *  </p>
 *  @see SyntaxRoutingService
 * @author sellidge
 * @author $Author: phigginson $
 */
public interface SyntaxIdentifier {

  /** Set the configured destination.
   *  <p>This is the value that will be stored against the metadata key
   *  specified by SyntaxRoutingService.
   * @param dest the destination.
   */
  void setDestination(String dest);

  /** Get the configured destination.
   *
   * @return the destination.
   */
  String getDestination();

  /** Add a pattern to the list of configured patterns used to match a
   *  document.
   * @param pattern a pattern.
   * @throws ServiceException if the pattern could not be added.
   */
  void addPattern(String pattern) throws ServiceException;

  /** Get a list of configured patterns.
   *
   * @return the list.
   */
  List<String> getPatterns();

  /** Set a list of configured patterns.
   *
   * @param l the list.
   */
  void setPatterns(List<String> l);

  /** Determine if this SyntaxIdentifer considers the message to
   *  match all the configured patterns.
   *
   * @param message the message to identify against.
   * @return true if it matches.
   * @throws ServiceException if there was an error with the pattern.
   */
  boolean isThisSyntax(String message) throws ServiceException;
}
