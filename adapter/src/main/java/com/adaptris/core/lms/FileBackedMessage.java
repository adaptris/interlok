package com.adaptris.core.lms;

import java.io.File;
import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;

/**
 * <p>
 * Represents a <i>message</i> in the framework which is backed by a file on the
 * filesystem.
 * </p>
 */
public interface FileBackedMessage extends AdaptrisMessage {


  /**
   * Initialise this AdaptrisMessage from an existing object.
   * 
   * @param sourceObject the source file to initialise from.
   * @throws IOException wrapping any access error.
   */
  void initialiseFrom(File sourceObject) throws IOException, RuntimeException;

  
  /**
   * Returns the current file that is the source of the message.
   * 
   * @return the current source file.
   */
  File currentSource();

}
