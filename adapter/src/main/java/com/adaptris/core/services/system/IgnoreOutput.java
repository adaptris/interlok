package com.adaptris.core.services.system;

import java.io.OutputStream;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.stream.DevNullOutputStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Null implementation of {@link CommandOutputCapture}
 * 
 * @config system-command-ignore-output
 * 
 * @author lchan
 * 
 */
@XStreamAlias("system-command-ignore-output")
public class IgnoreOutput implements CommandOutputCapture {

  @Override
  public OutputStream startCapture(AdaptrisMessage msg) {
    return new DevNullOutputStream();
  }

}
