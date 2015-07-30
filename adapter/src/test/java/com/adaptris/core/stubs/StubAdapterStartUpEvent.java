package com.adaptris.core.stubs;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdapterStartUpEvent;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Event containing <code>Adapter</code> start-up information..
 * </p>
 *
 * @see AdapterStartUpEvent
 */
@XStreamAlias("stub-adapter-startup-event")
public class StubAdapterStartUpEvent extends AdapterStartUpEvent {
  private static final long serialVersionUID = 2014012301L;

  public StubAdapterStartUpEvent() throws Exception {
  }

  /**
   *
   * @see AdapterStartUpEvent#setAdapter(Adapter)
   */
  @Override
  public void setAdapter(Adapter param) throws CoreException {
  }
}
