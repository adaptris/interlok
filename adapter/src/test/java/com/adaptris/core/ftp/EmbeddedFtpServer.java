package com.adaptris.core.ftp;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;

public class EmbeddedFtpServer {

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
      // assertEquals(PAYLOAD, m.getStringPayload().trim());
    }
  }

}
