package com.adaptris.core.management.config;

import com.adaptris.core.AdapterXStreamMarshallerFactory;
import com.adaptris.core.AdapterMarshallerFactory;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.AdapterXStreamMarshallerFactory.OutputMode;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.Constants;

/**
 * Implementation of the {@link AdapterConfigManager} interface for XStream.
 *
 * @author gcsiki
 *
 */
public class XStreamConfigManager extends ReadWriteConfigManager {

	protected AdapterMarshallerFactory marshallerFactory;
	
  public XStreamConfigManager() throws Exception {
    super();
	}

  @Override
  public void configure(BootstrapProperties bootstrapProperties) throws Exception {
    this.bootstrapProperties = bootstrapProperties;
    
    // Get the configured output type property (XML/JSON) and create the marshaller based on this
    final String marshallerOutputProperty = 
        (bootstrapProperties != null ?
            BootstrapProperties
            .getPropertyIgnoringCase(bootstrapProperties,
                Constants.CFG_KEY_MARSHALLER_OUTPUT_TYPE) : null);
    
    // Get the xstream enable beautified output flag
    final boolean enableBeautifiedOutputFlag = Boolean.valueOf(
        (bootstrapProperties != null ?
            BootstrapProperties
            .getPropertyIgnoringCase(bootstrapProperties,
                Constants.CFG_XSTREAM_BEAUTIFIED_OUTPUT) : null)
                );
    
    // Now initialize the marshaller
    marshallerFactory = AdapterXStreamMarshallerFactory.getInstance();
    if (enableBeautifiedOutputFlag) {
      ((AdapterXStreamMarshallerFactory)marshallerFactory).setMode(OutputMode.ALIASED_SUBCLASSES);
    }
    marshaller = marshallerFactory.createMarshaller(marshallerOutputProperty);
    DefaultMarshaller.setDefaultMarshaller(marshaller);
  }
  
  @Override
  public String getDefaultAdapterConfig() {
    return Constants.DEFAULT_XSTREAM_RESOURCE_NAME;
  }

  @Override
  protected String getDefaultAdapterResourceName() {
    return getDefaultAdapterConfig();
  }

}
