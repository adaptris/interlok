package com.adaptris.core.services.exception;


/**
 * {@link ExceptionGenerator} implementation that generates the exception from metadata.
 * 
 * @author lchan
 * 
 */
public abstract class ExceptionFromMetadataImpl implements ExceptionGenerator {

  private String exceptionMessageMetadataKey;

  public ExceptionFromMetadataImpl() {
  }

  /**
   * Returns the metadata key against which an exception message is expected at
   * runtime. <b>if a key is configured and a value is stored against it this
   * message is used instead of any configured value</b>
   *
   * @return the metadata key against which an exception message is expected at
   *         runtime
   */
  public String getExceptionMessageMetadataKey() {
    return exceptionMessageMetadataKey;
  }

  /**
   * Sets the metadata key against which an exception message is expected at
   * runtime.
   *
   * @param s the metadata key against which an exception message is expected at
   *          runtime
   */
  public void setExceptionMessageMetadataKey(String s) {
    exceptionMessageMetadataKey = s;
  }
}
