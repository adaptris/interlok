package com.adaptris.tester.runtime.helpers;

public abstract class PortProvider {

  private static final int DEFAULT_PORT = 8080;

  private int port;

  public PortProvider(){
    setPort(DEFAULT_PORT);
  }

  public void initPort(){

  }

  public final int getPort(){
    return this.port;
  }

  public final void setPort(int port) {
    this.port = port;
  }

  public void releasePort(){

  }
}
