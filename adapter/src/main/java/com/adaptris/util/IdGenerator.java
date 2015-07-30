/*
 * $Author: lchan $
 * $RCSfile: IdGenerator.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/18 15:40:31 $
 */
package com.adaptris.util;


/** Interface for ID Generation.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface IdGenerator {

  /** Create a unique id based on the object.
   * 
   * @param msg the object to create an id around
   * @return a unique id for use.
   */
  String create(Object msg);
}
