package com.adaptris.core.stubs; 

import com.adaptris.core.Adapter;


/**
 * <p>
 * Example of how to use <code>EventHandlerWrapperStub</code>.
 * </p>
 */
public abstract class EventHandlerWrapperStubExample {
  
  private EventHandlerWrapperStubExample() {
    // no instances
  }

  public static void main(String[] args) throws Exception {
    EventHandlerWrapperStub stub = new EventHandlerWrapperStub();
    Adapter adapter = stub.requestAdapter("destinationid", "sourceid");

    System.out.println(adapter);
  }
}