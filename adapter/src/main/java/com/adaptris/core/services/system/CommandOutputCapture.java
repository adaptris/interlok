package com.adaptris.core.services.system;

import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for capturing output from a process.
 * 
 * @author lchan
 * 
 */
public interface CommandOutputCapture {

  /**
   * Capture the output from the process.
   * 
   * @param msg the adaptris message.
   * @return an OutputStream capturing the command output.
   * @throws IOException if an outputstream couldn't be created.
   */
  OutputStream startCapture(AdaptrisMessage msg) throws IOException;
}
