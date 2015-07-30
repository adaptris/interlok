/*
 * $Id: Target.java,v 1.4 2006/06/12 07:50:12 lchan Exp $
 */
package com.adaptris.transform;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.stream.StreamResult;
/**
 * Create an instance of this class to provide the transformation
 * method a container for the result of a transformation.
 *
 * <p>When using a set method to set the internal state of a
 * <code>Target</code> object, the most recent set method will unset
 * the effect of any previous set invocation. In other words repeated
 * set method invocations are not cumulative in their effect.</p>
 *
 * @author   Trevor Vaughan
 * @version  0.1 April 2001
 */
public class Target {

  // //////////////////////////////////////
  // internal state
  // //////////////////////////////////////
  private StreamResult target;
  private String url = null;
  private Writer charStream = null;
  private OutputStream byteStream = null;

  // //////////////////////////////////////
  // constructors
  // //////////////////////////////////////

  /**
   * <p>Zero-argument default constructor. Before you can use
   * a <code>Target</code> in a transformation, you must define
   * the output container by setting its one of its properties.</p>
   *
   * @see #setFileName(String)
   * @see #setCharStream(Writer)
   * @see #setByteStream(OutputStream)
   */
  public Target() {
    this.target = new StreamResult();
  }

  /**
   * <p>
   * Create a new <code>Target</code> using the file name. For example...
   * </p>
   * 
   * <pre>
   * {@code 
   *  Target t = new Target("temp/myFile.xml");
   * }
   * </pre>
   * 
   * @param fileName the name of the file as a string.
   */
  public Target(String fileName) {
    this();
    _setFileName(fileName);
  }

  /**
   * <p>
   * Creates a new <code>Target</code> using the character stream. For example...
   * </p>
   * 
   * <pre>
   * {@code 
   *  StringWriter stringWriter = new StringWriter();
   *
   *  Target t = new Target(stringWriter);
   *
   *         (pass Target reference for processing...)
   *
   *  System.out.println("The result of the transformation is...");
   *  System.out.println(stringWriter.toString());
   *  }
   * </pre>
   * 
   * @param charStream the output character stream to wrap.
   */
  public Target(Writer charStream) {
    this();
    _setCharStream(charStream);
  }

  /**
   * <p>
   * Creates a new <code>Target</code> using the byte stream.
   * </p>
   * 
   * <pre>
   * {@code
   *  FileOutputStream out = new FileOutputStream("temp/myFile.xml");
   *
   *  Target t = new Target(out);
   *
   *         (process code here)
   * }
   * </pre>
   * 
   * @param byteStream the output byte stream to wrap.
   */
  public Target(OutputStream byteStream) {
    this();
    _setByteStream(byteStream);
  }

  /**
   * <p>Creates a new <code>Target</code> using a <code>XSLTResultTarget</code>.
   * </p>
   *
   * @param result the output <code>XSLTResultTarget</code> to wrap.
   */
  public Target(StreamResult result) {
    this();
    _setStreamResult(result);
  }

  /*
  *********************
  consider having more constructors
  
  what about Node and DocumentHandler which is part of the XSLTResultTarget 
  class?
  
  *********************
  */

  // //////////////////////////////////////
  //  set methods
  // //////////////////////////////////////

  /**
   * <p>Sets the <code>Target</code> using the file name.</p>
   *
   * @param fileName the name of the file as a string.
   */
  public void setFileName(String fileName) {
    _setCharStream(null);
    _setByteStream(null);
    _setFileName(fileName);
  }

  /**
   * <p>Sets the <code>Target</code> using the character stream.</p>
   *
   * @param charStream the output character stream to wrap.
   */
  public void setCharStream(Writer charStream) {
    _setFileName(null);
    _setByteStream(null);
    _setCharStream(charStream);
  }

  /**
   * <p>Sets the <code>Target</code> using the byte stream.</p>
   *
   * @param byteStream the output byte stream to warp.
   */
  public void setByteStream(OutputStream byteStream) {
    _setFileName(null);
    _setCharStream(null);
    _setByteStream(byteStream);
  }

  /**
   * <p>Sets the <code>Target</code> using a <code>XSLTResultTarget</code>.</p>
   *
   * @param result the output <code>XSLTResultTarget</code> to wrap.
   */
  public void setStreamResult(StreamResult result) {
    _setStreamResult(result);
  }

  // //////////////////////////////////////
  //  get methods
  // //////////////////////////////////////

  /* Commented Out as not required
   * <p>Returns a reference to the object's internal state as a
   * <code>XSLTResultTarget</code>.</p>
   *
  public XSLTResultTarget getXSLTResultTarget()
  {
    return this.target;
  }
   **/

  /**
   * <p>Returns a reference to the object's internal state as a
   * <code>StreamResult</code>.</p>
   * @return the stream result.
   */
  public StreamResult getStreamResult() {
    return target;
  }

  /**
   * <p>Returns a reference to the object's internal state as a
   * <code>Writer</code>.</p>
   *
   * @throws IOException when an error is detected creating the <code>Writer
   * </code>.
   * @return the writer.
   */
  public Writer getWriter() throws IOException {
    Writer writer = null;
    OutputStream tempOs = null;
    Writer tempWriter = null;
    String tempString = null;

    if ((tempOs = target.getOutputStream()) != null) {
      writer = new BufferedWriter(new OutputStreamWriter(tempOs));
    } else if ((tempWriter = target.getWriter()) != null) {
      writer = new BufferedWriter(tempWriter);
    } else if ((tempString = this.target.getSystemId()) != null) {
      writer =
        new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(tempString)));
    }

    return writer;
  }

  // //////////////////////////////////////
  //  other  methods
  // //////////////////////////////////////

  /**
   * <p>Resets the object. The internal state of the object is set
   * to <code>null</code>.</p>
   */
  public final void reset() {
    target = new StreamResult();
  }

  /**@see Object#toString()
   */
  public String toString() {
    return (
      getClass().getName()
        + ':'
        + this.target
        + '@'
        + Integer.toHexString(hashCode()));
  }

  // //////////////////////////////////////
  //  private  methods
  // //////////////////////////////////////

  private void _setFileName(String fileName) {
    this.url = fileName;
    target.setSystemId(fileName);
  }

  private void _setCharStream(Writer charStream) {
    this.charStream = charStream;
    this.target.setWriter(charStream);
  }

  private void _setByteStream(OutputStream byteStream) {
    this.byteStream = byteStream;
    this.target.setOutputStream(byteStream);
  }

  private void _setStreamResult(StreamResult result) {
    this.target = result;
  }

} // class Target
