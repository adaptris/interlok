package com.adaptris.tester.runtime.helpers;

import com.adaptris.tester.runtime.ServiceTestException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@XStreamAlias("wire-mock-helper")
public class WireMockHelper implements Helper {

  private static final String WIRE_MOCK_HELPER_PORT_PROPERTY_NAME = "wire.mock.helper.port";

  private transient WireMockServer wireMockServer;

  private String fileSource;

  private PortProvider portProvider;

  private Map<String, String> helperProperties;

  public WireMockHelper(){
    setPortProvider(new StaticPortProvider());
    helperProperties = new HashMap<>();
  }

  @Override
  public void init() throws ServiceTestException {
    portProvider.initPort();
    helperProperties.put(WIRE_MOCK_HELPER_PORT_PROPERTY_NAME, String.valueOf(portProvider.getPort()));
    wireMockServer = new WireMockServer(portProvider.getPort(), new SingleRootFileSource(fileSource), false);
    wireMockServer.start();
  }

  @Override
  public void close() throws IOException {
    wireMockServer.stop();
    portProvider.releasePort();
  }

  @Override
  public Map<String, String> getHelperProperties() {
    return helperProperties;
  }

  public String getFileSource() {
    return fileSource;
  }

  public void setFileSource(String fileSource) {
    this.fileSource = fileSource;
  }

  public PortProvider getPortProvider() {
    return portProvider;
  }

  public void setPortProvider(PortProvider portProvider) {
    this.portProvider = portProvider;
  }
}
