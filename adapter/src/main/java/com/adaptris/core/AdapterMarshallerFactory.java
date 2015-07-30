package com.adaptris.core;

import org.apache.commons.lang.StringUtils;

/**
 * Factory Class that creates the required Marshaller ie the DataBinder instance that allows
 * us to marshal & unmarshal our objects eg XStream, Castor etc<br />
 * This factory also allows the DataBinder to be configured to output to a given format eg JSON/XML. 
 * 
 * @author bklair
 */
public abstract class AdapterMarshallerFactory {

  // Defined valid Marshaller Output Format Types
  public static enum MarshallingOutput {XML, JSON};

  // Create a default marshaller - this outputs to XML
  public abstract AdaptrisMarshaller createMarshaller() throws CoreException;

  // Create a marshaller that generates the given output type
  public abstract AdaptrisMarshaller createMarshaller(
      MarshallingOutput outputType) throws CoreException;

  /**
   * Creates an AdaptrisMarshaller based on the passed in config property which
   * specifies the output format to use. This will default to XML if the
   * parameter passed in is null or invalid.
   * 
   * @param configuredOutputTypeProperty - Property value from config file that
   *          is a valid {@link MarshallingOutput} enum or null.
   * @return - new configured AdaptrisMarshaller instance
   * @throws CoreException
   */
  public AdaptrisMarshaller createMarshaller(String configuredOutputTypeProperty)
      throws CoreException {
    if (StringUtils.isBlank(configuredOutputTypeProperty)) {
      return createMarshaller();
    }

    // Now convert the String property to a MarshallingOutput enum value,
    // default to XML if there is an exception
    MarshallingOutput configuredMarshallingOutput;
    try {
      configuredMarshallingOutput = MarshallingOutput
          .valueOf(configuredOutputTypeProperty);
    } catch (IllegalArgumentException iae) {
      configuredMarshallingOutput = MarshallingOutput.XML;
    }

    return createMarshaller(configuredMarshallingOutput);
  }

}
