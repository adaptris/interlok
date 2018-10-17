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

package com.adaptris.fs;

import java.io.File;
import java.io.FileFilter;

/**
 * <p>
 * Defines basic file system operations. Implementations may be based on <code>java.io</code>, <code>java.nio</code>, etc., etc.
 * This interface is currently envisaged for local file system use only.
 * </p>
 */
public interface FsWorker {

  /**
   * <p>
   * Returns an array of <code>File</code>s in directory <code>dir</code>. If <code>dir</code> does not exist, if <code>dir</code>
   * is a file, if <code>dir</code> has not got appropriate permissions, or if <code>dir</code> is null a <code>FsException</code>
   * will be thrown.
   * </p>
   *
   * @param dir the directory to list
   * @return an array of <code>File</code>s
   * @throws FsException wrapping any underlying Exception
   */
  File[] listFiles(File dir) throws FsException;

  /**
   * <p>
   * Returns an array of <code>File</code>s in directory <code>dir</code> that match the passed <code>filter</code>. If
   * <code>dir</code> does not exist, if <code>dir</code> is a file, if <code>dir</code> has not got appropriate permissions, or if
   * <code>dir</code> is null a <code>FsException</code> will be thrown.
   * </p>
   *
   * @param dir the directory to list
   * @param filter the <code>FileFilter</code> to apply
   * @return an array of <code>File</code>s
   * @throws FsException wrapping any underlying Exception
   */
  File[] listFiles(File dir, FileFilter filter) throws FsException;

  /**
   * <p>
   * Gets the contents of the specified <code>File</code>. If the file doesn't exist or other I/O problems are encountered a
   * <code>FsException</code> will be thrown.
   * </p>
   *
   * @param file the <code>File</code> to get
   * @return the contents of the file
   * @throws FsException wrapping any underlying Exception that may occur
   */
  byte[] get(File file) throws FsException;

  /**
   * <p>
   * Writes <code>data</code> to the specified <code>file</code>. If the named file already exists or if part of the path doesn't
   * exist a <code>FsException</code> is thrown.
   * </p>
   *
   * @param data the data to write
   * @param file the file to write to
   * @throws FsException wrapping any underlying Exception that may occur
   */
  void put(byte[] data, File file) throws FsException;

  /**
   * <p>
   * Renames <code>oldfile</code> to <code>newName</code>. If <code>oldFile</code> does not exist or is a directory, or if
   * <code>newName</code> already exists an <code>FsException</code> is thrown.
   * </p>
   * 
   * @param oldFile the file to rename
   * @param newName the new name for the file
   * @throws FsFilenameExistsException if newName exists
   * @throws FsException wrapping any underlying Exception that may occur
   */
  void rename(File oldFile, File newName) throws FsException, FsFilenameExistsException;

  /**
   * <p>
   * Deletes the specified <code>file</code>. If the file doesn't exist returns quietly.
   * </p>
   *
   * @param file the file to delete in the
   * @throws FsException wrapping any underlying Exception that may occur
   */
  void delete(File file) throws FsException;

  /**
   * <p>
   * Returns true if the passed <code>File</code> is a writeable directory, otherwise false.
   * </p>
   *
   * @param dir the directory to check in the
   * @return true if the passed <code>File</code> is a writeable directory, otherwise false
   * @throws FsException wrapping any underlying Exception that may occur
   */
  boolean isWriteableDir(File dir) throws FsException;
}
