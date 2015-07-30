package com.adaptris.core;

/**
 * Extension to {@link StateManagedComponent} used for fine grained state transitions.
 * <p>
 * A {@link StateManagedComponent} may have references to other <code>StateManagedComponent</code>s whose state maybe be
 * independently controlled and thus different from the state of this object. Therefore in some instances we still need to delegate
 * requests to any state managed sub-components in case action is required.
 * </p>
 * <p>
 * For instance, a JMX request stops a channel. In this instance, the adapter is still considered started; if an invocation of
 * {@link StateManagedComponent#requestStart()} occurs for the adapter, it cannot just do nothing, it must delegate operations to
 * any children that will require it.
 * </p>
 */
public interface StateManagedComponentContainer extends StateManagedComponent {

  /**
   * Initialise any independent sub-components.
   *
   * @throws CoreException wrapping any underlying Exceptions
   */
  void requestChildInit() throws CoreException;

  /**
   * Start any independent sub components.
   *
   * @throws CoreException wrapping any underlying Exceptions
   */
  void requestChildStart() throws CoreException;

  /**
   * Stop any independent sub components.
   */
  void requestChildStop();

  /**
   * Close any independent sub components.
   */
  void requestChildClose();
}
