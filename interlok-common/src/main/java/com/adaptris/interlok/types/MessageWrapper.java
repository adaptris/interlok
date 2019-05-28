package com.adaptris.interlok.types;

/**
 * @since 3.9.0
 */
@FunctionalInterface
public interface MessageWrapper<T> {
  /**
   * Wrap the interlok message into something else.
   * 
   * @param m the message
   * @return the wrapped instance.
   * @throws Exception on exception.
   */
  T wrap(InterlokMessage m) throws Exception;
}
