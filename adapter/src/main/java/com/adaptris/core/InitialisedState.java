/*
 * $RCSfile: InitialisedState.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

/**
 * <p>
 * Represents initialised <code>StateManagedComponent</code>s and implements permitted transitions.
 * </p>
 */
public final class InitialisedState extends ComponentStateImp {
  private static final long serialVersionUID = 2012022101L;
  private static InitialisedState instance;

  /**
   * @see com.adaptris.core.ComponentState#requestStart (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestStart(StateManagedComponent comp) throws CoreException {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        comp.start();
        logActivity("Started", comp);
        comp.changeState(StartedState.getInstance());
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
        comp.close();
        logActivity("Closed", comp);
        comp.changeState(ClosedState.getInstance());
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
  public static InitialisedState getInstance() {
    if (instance == null) {
      instance = new InitialisedState();
    }

    return instance;
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  private InitialisedState() {
    // na
  }
}
