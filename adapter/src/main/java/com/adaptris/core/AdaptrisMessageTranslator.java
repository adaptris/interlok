package com.adaptris.core;


/**
 * Base interface for translating messages to and from AdaptrisMessage
 * instances.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface AdaptrisMessageTranslator {
  /**
   * Register the message factory that should be used to create messages.
   * 
   * @param f the message factory.
   */
  void registerMessageFactory(AdaptrisMessageFactory f);

  /**
   * Return the currently registered message factory.
   * 
   * @return the message factory.
   */
  AdaptrisMessageFactory currentMessageFactory();
}
