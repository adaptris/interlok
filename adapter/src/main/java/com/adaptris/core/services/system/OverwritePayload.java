package com.adaptris.core.services.system;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CommandOutputCapture} that overwrites the existing message with the output.
 * 
 * @author lchan
 * 
 */
@XStreamAlias("system-command-overwrite-payload")
public class OverwritePayload implements CommandOutputCapture {

  @Override
  public OutputStream startCapture(AdaptrisMessage msg) throws IOException {
    return new BufferedOutputStream(msg.getOutputStream());
  }

}
