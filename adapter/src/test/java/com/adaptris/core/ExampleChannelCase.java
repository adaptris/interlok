package com.adaptris.core;

import java.util.UUID;

import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.metadata.AddMetadataService;

/**
 * <p>
 * Extension to <code>BaseCase</code> for <code>Service</code>s which
 * provides a method for marshaling sample XML config.
 * </p>
 */
public abstract class ExampleChannelCase extends ExampleConfigCase {
  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "ChannelCase.baseDir";

  public ExampleChannelCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);
    ChannelList w = (ChannelList) object;

    result = result + configMarshaller.marshal(w);
    return result;
  }

  private ServiceCollection createServiceCollection() throws CoreException {
    ServiceCollection services = new ServiceList();
    AddMetadataService service = new AddMetadataService();
    service.addMetadataElement("key1", "val1");
    services.addService(service);
    services.addService(new LogMessageService());
    return services;
  }


  protected Workflow createDefaultWorkflow() throws CoreException {
    return configureWorkflow(new StandardWorkflow());
  }

  protected Workflow configureWorkflow(WorkflowImp impl) throws CoreException {
    impl.setUniqueId(UUID.randomUUID().toString());
    ConfiguredConsumeDestination dest = new ConfiguredConsumeDestination("dummy");
    AdaptrisMessageConsumer consumer = new NullMessageConsumer();
    consumer.setDestination(dest);
    impl.setConsumer(consumer);
    impl.setProducer(new NullMessageProducer());
    impl.setServiceCollection(createServiceCollection());
    return impl;
  }

}
