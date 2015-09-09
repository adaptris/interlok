package com.adaptris.core;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Holder for example <code>Service</code>s so that xsi:type is output in XML.
 * </p>
 */
@XStreamAlias("dummy-placeholder-service-element")
public class ExampleServiceConfig {

  // Note that this is a list so that it comes out nice in XSTream.
  // so we get
  // <dummy-placeholder-service-element>
  // <add-metadata-service>
  // </add-metadata-service>
  // </dummy-placeholder-service-element>
  // rather than
  // <dummy-placeholder-service-element>
  // <service class="add-metadata-service">
  // </service>
  // </dummy-placeholder-service-element>
  private List<Service> services = new ArrayList<Service>();

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> s) {
    services = s;

  }

  public void addService(Service s) {
    services.add(s);
  }
}
