/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.management.config;
import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;
import com.adaptris.core.AdapterMarshallerFactory;
import com.adaptris.core.AdapterXStreamMarshallerFactory;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.Constants;
import com.adaptris.core.util.Args;

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
    this.bootstrapProperties = Args.notNull(bootstrapProperties, "bootstrapProperties");
    
    // Get the configured output type property (XML/JSON) and create the marshaller based on this
    final String marshallerOutputProperty = getPropertyIgnoringCase(bootstrapProperties, Constants.CFG_KEY_MARSHALLER_OUTPUT_TYPE);

    // Now initialize the marshaller
    marshallerFactory = AdapterXStreamMarshallerFactory.getInstance();
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
