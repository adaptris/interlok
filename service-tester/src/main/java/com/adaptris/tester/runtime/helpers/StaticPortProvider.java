package com.adaptris.tester.runtime.helpers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("static-port-provider")
public class StaticPortProvider implements PortProvider {

  private static final int DEFAULT_PORT = 8080;

  private int port;

  public StaticPortProvider(){
    setPort(DEFAULT_PORT);
  }

  @Override
  public void initPort() {

  }

  @Override
  public int getPort() {
    return this.port;
  }

  public void setPort(int port){
    this.port = port;
  }

  @Override
  public void releasePort() {

  }
}
