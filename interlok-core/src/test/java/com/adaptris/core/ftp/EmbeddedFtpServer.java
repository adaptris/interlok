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

package com.adaptris.core.ftp;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.command.AbstractFakeCommandHandler;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;

@SuppressWarnings("deprecation")
public class EmbeddedFtpServer {

  private static final String MDTM = "mdtm";

  public static final String SLASH = "/";
  public static final String DEFAULT_PASSWORD = "password";
  public static final String DEFAULT_USERNAME = "user";
  public static final String DEFAULT_HOME_DIR = "/home/user";
  public static final String DEFAULT_WORK_DIR_NAME = "work";
  public static final String DEFAULT_BUILD_DIR_NAME = "build";
  public static final String DEFAULT_PROC_DIR_NAME = "proc";
  public static final String DEFAULT_REPLY_DIR_NAME = "reply";
  public static final String PAYLOAD = "Quick zephyrs blow, vexing daft Jim";
  public static final String PAYLOAD_ALTERNATE = "Pack my box with five dozen liquor jugs";

  public static final String DEFAULT_WORK_DIR_CANONICAL = DEFAULT_HOME_DIR + SLASH + DEFAULT_WORK_DIR_NAME;
  public static final String DEFAULT_BUILD_DIR_CANONICAL = DEFAULT_HOME_DIR + SLASH + DEFAULT_BUILD_DIR_NAME;
  public static final String DEFAULT_PROC_DIR_CANONICAL = DEFAULT_HOME_DIR + SLASH + DEFAULT_PROC_DIR_NAME;
  public static final String DEFAULT_REPLY_DIR_CANONICAL = DEFAULT_HOME_DIR + SLASH + DEFAULT_REPLY_DIR_NAME;
  public static final String DEFAULT_FILENAME = "file";

  public static final String DESTINATION_URL_OVERRIDE = "ftp://user:password@localhost" + DEFAULT_HOME_DIR;
  public static final String DESTINATION_URL = "ftp://localhost" + DEFAULT_HOME_DIR;
  public static final String SERVER_ADDRESS = "localhost";

  public FakeFtpServer createAndStart(FileSystem filesystem) {
    return createAndStart(filesystem, new UserAccount(DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_HOME_DIR));
  }

  public FakeFtpServer createAndStart(FileSystem filesystem, UserAccount account) {
    FakeFtpServer server = new FakeFtpServer();
    server.setCommandHandler("MDTM", new MdtmCommandHandler());
    server.setServerControlPort(0);
    server.addUserAccount(account);
    server.setFileSystem(filesystem);
    server.start();
    return server;
  }

  public FileSystem createFilesystem(int noFiles) {
    return createFilesystem(noFiles, DEFAULT_WORK_DIR_NAME, DEFAULT_BUILD_DIR_NAME, DEFAULT_PROC_DIR_NAME);
  }

  public FileSystem createFilesystem(int noFiles, String... dirsToCreate) {
    List<FileSystemEntry> files = new ArrayList<FileSystemEntry>();
    for (String dir : dirsToCreate) {
      files.add(new DirectoryEntry(DEFAULT_HOME_DIR + SLASH + dir));
    }
    for (int i = 0; i < noFiles; i++) {
      FileEntry entry = new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i, PAYLOAD);
      entry.setLastModified(new Date());
      files.add(entry);
    }
    return createFilesystem(files);
  }

  public FileSystem createFilesystem_DirsOnly() {
    return createFilesystem_DirsOnly(DEFAULT_WORK_DIR_NAME, DEFAULT_BUILD_DIR_NAME, DEFAULT_PROC_DIR_NAME);
  }

  public FileSystem createFilesystem_DirsOnly(String... dirsToCreate) {
    List<FileSystemEntry> files = new ArrayList<FileSystemEntry>();
    for (String dir : dirsToCreate) {
      files.add(new DirectoryEntry(DEFAULT_HOME_DIR + SLASH + dir));
    }
    return createFilesystem(files);
  }

  public FileSystem createFilesystem(List<FileSystemEntry> files) {
    UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
    fileSystem.setCreateParentDirectoriesAutomatically(true);
    for (FileSystemEntry entry : files) {
      fileSystem.add(entry);
    }
    return fileSystem;
  }

  public void assertMessages(List<AdaptrisMessage> list, int count) {
    // assertEquals("All files consumed/produced", count, list.size());
    for (AdaptrisMessage m : list) {
      assertTrue(m.containsKey(CoreConstants.ORIGINAL_NAME_KEY));
      // assertEquals(PAYLOAD, m.getContent().trim());
    }
  }

  public class MdtmCommandHandler extends AbstractFakeCommandHandler {

    public MdtmCommandHandler() {
    }

    protected void handle(Command command, Session session) {
      SimpleDateFormat tsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      verifyLoggedIn(session);
      String path = getRealPath(session, command.getRequiredParameter(0));
      replyCodeForFileSystemException = ReplyCodes.READ_FILE_ERROR;
      verifyFileSystemCondition(getFileSystem().isFile(path), path, "filesystem.isNotAFile");
      verifyReadPermission(session, path);
      FileSystemEntry entry = getFileSystem().getEntry(path);
      String result = tsFormat.format(entry.getLastModified());
      ArrayList args = new ArrayList();
      args.add(result);
      sendReply(session, 213, MDTM, args);
    }

    // This is to override with the mdtm resource bundle w/o having to change the resource bundle
    public ResourceBundle getReplyTextBundle() {
      return wrap(super.getReplyTextBundle());
    }

    private ResourceBundle wrap(final ResourceBundle bundle) {
      return new ResourceBundle() {

        @Override
        protected Object handleGetObject(String key) {
          if (MDTM.equalsIgnoreCase(key)) {
            return "{0}";
          }
          return bundle.getObject(key);
        }

        @Override
        public Enumeration<String> getKeys() {
          List<String> list = Collections.list(bundle.getKeys());
          list.add(MDTM);
          return Collections.enumeration(list);
        }
      };
    }
  }


}
