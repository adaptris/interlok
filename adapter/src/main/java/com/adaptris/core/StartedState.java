/*
 * $RCSfile: StartedState.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

/**
 * <p>
 * Represents started <code>StateManagedComponent</code>s and implements permitted transitions.
 * </p>
 */
public final class StartedState extends ComponentStateImp {
  private static final long serialVersionUID = 2012022101L;
  private static StartedState instance;

  /**
   * @see com.adaptris.core.ComponentState#requestStop (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestStop(StateManagedComponent comp) {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        comp.stop();
        logActivity("Stopped", comp);
        comp.changeState(StoppedState.getInstance());
      }
    }
  }

  /**
   * @see com.adaptris.core.ComponentState#requestClose (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestClose(StateManagedComponent comp) {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        requestStop(comp);

        comp.close();
        logActivity("Closed", comp);
        comp.changeState(ClosedState.getInstance());
      }
    }
  }

  /**
   * @see com.adaptris.core.ComponentState #requestInit(com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestInit(StateManagedComponent comp) throws CoreException {
    logActivity("Ignoring requestInit()", comp);
  }

  /**
   * <p>
   * Returns the single instance of this class.
   * </p>
   * 
   * @return the single instance of this class
   */
  public static StartedState getInstance() {
    if (instance == null) {
      instance = new StartedState();
    }

    return instance;
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  private StartedState() {
    // na
  }
}
