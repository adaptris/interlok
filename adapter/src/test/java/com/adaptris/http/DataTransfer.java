/*
 * $Id: DataTransfer.java,v 1.3 2003/10/27 07:10:26 lchan Exp $
 */
package com.adaptris.http;

import java.io.InputStream;
import java.io.OutputStream;

/** A Generic interface for http read and writes.
 */
public interface DataTransfer {
  /** Write to the supplied outputstream.
   *  @param out the outputstream
   *  @throws HttpException on exception
   */
  void writeTo(OutputStream out) throws HttpException;
 
  /** Load from the supplied inputstream.
   *  @param in the input stream
   *  @throws HttpException on exception
   */
  void load(InputStream in) throws HttpException;
}