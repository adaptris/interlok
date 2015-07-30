package com.adaptris.core.event;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdapterStartUpEvent;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Event containing <code>Adapter</code> start-up information..
 * </p>
 * 
 * @config standard-adapter-start-up-event
 * 
 * @see AdapterStartUpEvent
 */
@XStreamAlias("standard-adapter-start-up-event")
public class StandardAdapterStartUpEvent extends AdapterStartUpEvent {
  private static final long serialVersionUID = 2014012301L;
  // Dummy for backwards compatibility with v2 events.
  // Do not remove until all V2 clients are removed.
  private String compressedAdapterXml;
  // Dummy for backwards compatibility with v2 events.
  // Do not remove until all V2 clients are removed.
  private String adapter;

  public StandardAdapterStartUpEvent() throws Exception {
    super();
  }


  /**
   *
   * @see AdapterStartUpEvent#setAdapter(Adapter)
   */
  @Override
  public void setAdapter(Adapter param) throws CoreException {
  }
}
