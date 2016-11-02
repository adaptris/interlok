package com.adaptris.tester.runtime.helpers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("static-port-provider")
public class StaticPortProvider extends PortProvider {

  private int port;

  @Override
  public void initPort() {
    setPort(port);
  }
}
