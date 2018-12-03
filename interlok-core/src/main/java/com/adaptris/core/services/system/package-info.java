/**
 * Implementations of {@link com.adaptris.core.Service} and supporting classes allow execution of
 * system commands.
 * <p>
 * You should take care when configuring these classes; it can present an audit trail issue when
 * used in combination with {@link com.adaptris.core.services.dynamic.DynamicServiceLocator} or
 * {@link com.adaptris.core.services.dynamic.DynamicServiceExecutor} if the command(s) that are
 * executed are not properly configured or validated. All commands will be executed with the current
 * users permissions and subject to the virtual machines security manager.
 * </p>
 */
package com.adaptris.core.services.system;
