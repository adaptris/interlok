/*
 * $RCSfile: ClosedState.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

/**
 * <p>
 * Represents closed <code>StateManagedComponent</code>s and implements permitted transitions.
 * </p>
 */
public final class ClosedState extends ComponentStateImp {

  private static final long serialVersionUID = 2012022101L;
  private static ClosedState instance;

  /**
   * @see com.adaptris.core.ComponentState#requestInit (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestInit(StateManagedComponent comp) throws CoreException {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        comp.init();
        logActivity("Initialised", comp);
        comp.changeState(InitialisedState.getInstance());
      }
    }
  }

  /**
   * @see com.adaptris.core.ComponentState#requestStart (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestStart(StateManagedComponent comp) throws CoreException {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        requestInit(comp);
        comp.start();
        logActivity("Started", comp);
        comp.changeState(StartedState.getInstance());
      }
    }
  }

  /**
   * <p>
   * Returns the single instance of this class.
   * </p>
   * 
   * @return the single instance of this class
   */
  public static ClosedState getInstance() {
    if (instance == null) {
      instance = new ClosedState();
    }

    return instance;
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  private ClosedState() {
    // na
  }
}
