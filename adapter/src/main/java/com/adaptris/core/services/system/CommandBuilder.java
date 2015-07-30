package com.adaptris.core.services.system;

import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Interface for building a process.
 * 
 * @author lchan
 * 
 */
public interface CommandBuilder {
  /**
   * Build a {@link ProcessBuilder} object from the available data.
   * 
   * @param msg the AdaptrisMessage
   * @return a {@link ProcessBuilder}
   * @throws CoreException wrapping any other exception.
   */
  CommandLine createCommandLine(AdaptrisMessage msg) throws CoreException;

  /**
   * Configure the {@link Executor}.
   * 
   * @param exec the Executor to configure.
   * @return the same executor.
   * @throws CoreException wrapping other exceptions.
   */
  Executor configure(Executor exec) throws CoreException;

  /**
   * Create the environment when executing the process.
   * 
   * @param msg the message.
   * @return the environment for {@link Executor#execute(CommandLine, Map)}. If null, then the default environment is used.
   * @throws CoreException wrapping other exceptions
   */
  Map<String, String> createEnvironment(AdaptrisMessage msg) throws CoreException;
}
