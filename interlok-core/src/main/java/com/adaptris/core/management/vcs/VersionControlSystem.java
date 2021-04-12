/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.management.vcs;

import java.io.File;
import java.util.List;


/**
 * Basic Version control System interface.
 * 
 * @author amcgrath
 * @since 3.0.3
 * 
 */
public interface VersionControlSystem {

  /**
   * Will perform an action on the remote repository to confirm that the url and credentials for the connection are valid.
   * 
   * @param remoteRepoUrl
   * @param workingCopyUrl
   * @return Revision number
   * @throws VcsException
   */
  public String testConnection(String remoteRepoUrl, File workingCopyUrl) throws VcsException;

  /**
   * Will perform a fresh checkout from the remote repository url to the local working copy url.
   * 
   * @param remoteRepoUrl
   * @param workingCopyUrl
   * @return Revision number
   * @throws VcsException
   */
  public String checkout(String remoteRepoUrl, File workingCopyUrl) throws VcsException;

  /**
   * Will perform a fresh checkout from the remote repository url to the local working copy url, to the specified revision number.
   * @param remoteRepoUrl
   * @param workingCopyUrl
   * @param revision
   * @return Revision number
   * @throws VcsException
   */
  public String checkout(String remoteRepoUrl, File workingCopyUrl, String revision) throws VcsException;

  /**
   * Will fetch and update yuour local working copy to the specified revision.
   * @param workingCopyUrl
   * @param revision
   * @return Revision number
   * @throws VcsException
   */
  public String update(File workingCopyUrl, String revision) throws VcsException;

  /**
   * Will fetch and update your local working copy with the latest changes from the remote repository.
   * @param workingCopyUrl
   * @return Revision number
   * @throws VcsException
   */
  public String update(File workingCopyUrl) throws VcsException;

  /**
   * Will send your changes to the remote repository, with the supplied commit message.
   * @param workingCopyUrl
   * @param commitMessage
   * @throws VcsException
   */
  public void commit(File workingCopyUrl, String commitMessage) throws VcsException;

  /**
   * Will recursively check directories and sub directories adding all files for commit to the remote repository.
   * @param workingCopyUrl
   * @throws VcsException
   */
  public void recursiveAdd(File workingCopyUrl) throws VcsException;

  /**
   * Add and commit the list of provided files to the remote repository. Useful when only some files
   * need to be committed.
   * 
   * @param workingCopyUrl
   * @param commitMessage
   * @param fileNames relative to the workingCopyUrl
   * @throws VcsException
   */
  public void addAndCommit(File workingCopyUrl, String commitMessage, String... fileNames) throws VcsException;

  /**
   * @return VCS Implementation Name
   */
  public String getImplementationName();

  /**
   * This method will return the remote repositories latest revision number/string/id.
   * @return Revision as a String
   */
  public String getRemoteRevision(String remoteRepoUrl, File workingCopyUrl) throws VcsException;

  /**
   * This method will return your local repositories current revision number/string/id.
   * @return Revision as a String
   */
  public String getLocalRevision(File workingCopyUrl) throws VcsException;

  /**
   * <p>
   * Will return a list of {@link RevisionHistoryItem}'s from the remote repository.
   * </p>
   * <p>
   * The results returned may also be limited by providing a limit &gt; zero. If your supplied limit
   * equals zero then the results will not be limited.
   * </p>
   * 
   * @param remoteRepoUrl
   * @return a list of {@link RevisionHistoryItem}s
   * @throws VcsException
   */
  public List<RevisionHistoryItem> getRemoteRevisionHistory(String remoteRepoUrl, File workingCopyUrl, int limit) throws VcsException;

}
