package com.adaptris.core.management.vcs;

import java.util.Properties;

import com.adaptris.core.management.BootstrapProperties;

public interface RuntimeVersionControl {

  /**
   * Return the name of the version control system this implementation is built for.
   * An example might be "Subversion", "cvs", "git" etc.
   * @return String
   */
  public String getImplementationName();
  
  /**
   * Updates the local repository with the remote repository changes.
   * @throws VcsException
   */
  public void update() throws VcsException;
  
  /**
   * Will checkout a fresh copy of the remote repository files.
   * @throws VcsException
   */
  public void checkout() throws VcsException;
  
  /**
   * Allow the Version Control System to configure itself through the Bootstrap Properties.
   * @param bootstrapProperties
   */
  public void setBootstrapProperties(BootstrapProperties bootstrapProperties);
  
  /**
   * Will return a fully configured low-level api should you wish to perform direct actions on the
   * VCS.
   * 
   * @param properties configuration for bootstrapping the VCS api
   * @return a configured minimal VCS API
   * @throws VcsException
   * @since 3.0.3
   */
  public VersionControlSystem getApi(Properties properties) throws VcsException;
  
}
