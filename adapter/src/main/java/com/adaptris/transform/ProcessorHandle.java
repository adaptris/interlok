/*
 * $Id: ProcessorHandle.java,v 1.3 2006/06/12 07:50:12 lchan Exp $
 */
package com.adaptris.transform;

/**
 * <p>This provides the facility for a user application to select
 * a XSLT processor and to set and query processor properties/features.
 * Note that instantiation of a <code>ProcessorHandle</code> does
 * <b>not</b> create the processor, but just specifies which processor
 * is to be used.</p>
 *
 * <p align="center"><b>Only Xalan from Apache is currently supported.</b></p>
 *
 * @author   Trevor Vaughan
 * @version  0.1 April 2001
 */
public class ProcessorHandle {

  private String processorName = "";

  // For the parsing stage that is always required by a processor.
  private boolean validate = false; // validate against a DTD?
  private boolean aware = false; // namespace aware?

  /**
   * <p>Zero-argument default constructor. Using this will result
   * in Xalan from Apache being selected.</p>
   */
  public ProcessorHandle() {
    _setProcessorName("xalan");
  }

  /**
   * <p>Selects a processor based on the fully qualified name of the
   * processor.</p>
   *
   * <p align="center"><b>This method is for future use and currently
   * is equivalent to invoking {@link #ProcessorHandle()}.</b></p>
   * @param name the name of the processor to use.
   */
  public ProcessorHandle(String name) {
    _setProcessorName("xalan");
    //_setProcessorName(name);
  }

  /**
   * <p>Sets validation on or off. If <code>true</code> is passed
   * then it indicates that validation should occur during the
   * parsing process that precedes the transformation. The default
   * setting for <code>ProcessorHandle</code> is <code>false</code>.</p>
   *
   * @see #isValidating()
   */
  public void setValidation(boolean validate) {
    this.validate = validate;
  }

  /**
   * <p>Sets the namespace awareness feature on or off. The default
   * setting for <code>ProcessorHandle</code> is <code>false</code>.</p>
   *
   * @see #isNamespaceAware()
   */
  public void setNamespaceAware(boolean aware) {
    this.aware = aware;
  }

  /**
   * <p>Returns the name of the processor.</p>
   * @return the name of the processor
   */
  public String getProcessorName() {
    return this.processorName;
  }

  /**
   * <p>Returns whether parser validation should be used in the
   * parsing stage that precedes a transformation.</p>
   *
   * @see #setValidation(boolean)
   */
  public boolean isValidating() {
    return this.validate;
  }

  /**
   * <p>Returns whether namespace awareness should be used in the
   * parsing stage that precedes a transformation.</p>
   *
   * @see #setNamespaceAware(boolean)
   */
  public boolean isNamespaceAware() {
    return this.aware;
  }

  /**
   *  @see java.lang.Object#toString()
   */
  public String toString() {
    return (
      getClass().getName()
        + ':'
        + getProcessorName()
        + '@'
        + Integer.toHexString(hashCode()));
  }

  private void _setProcessorName(String processorName) {
    if (processorName != null)
      this.processorName = processorName.trim().toLowerCase();
    else
      this.processorName = "";
  }

} // class ProcessorHandle
