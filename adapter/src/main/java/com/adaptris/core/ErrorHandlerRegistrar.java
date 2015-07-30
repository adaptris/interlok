package com.adaptris.core;

import com.adaptris.core.runtime.MessageErrorDigester;

/**
 * <p>
 * <code>ErrorHandlerRegister</code>
 * </p>
 *
 * @author Aaron - 19 Nov 2012
 * @version 1.0
 *
 */
public interface ErrorHandlerRegistrar {

	public void registerParent(ProcessingExceptionHandler handler);

  public void registerDigester(MessageErrorDigester digester);

	public void notifyParent(AdaptrisMessage message);

	public void onChildError(AdaptrisMessage message);

}
