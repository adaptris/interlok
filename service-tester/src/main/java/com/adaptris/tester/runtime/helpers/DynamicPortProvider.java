package com.adaptris.tester.runtime.helpers;

import com.adaptris.core.PortManager;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dynamic-port-provider")
public class DynamicPortProvider extends PortProvider {

  private static final int DEFAULT_PORT_OFFSET = 8080;

  private int offset;


  public DynamicPortProvider(){
    this(DEFAULT_PORT_OFFSET);
  }

  public DynamicPortProvider(int offset){
    setOffset(offset);
  }


  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getOffset() {
    return offset;
  }

  @Override
  public void initPort() {
    setPort(PortManager.nextUnusedPort(getOffset()));
  }

  @Override
  public void releasePort() {
    PortManager.release(getPort());
  }
}
