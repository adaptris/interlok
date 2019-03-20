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

import com.adaptris.core.util.Args;

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
  default File[] listFiles(File dir) throws FsException {
    return listFiles(dir, null);
  }

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
  default File[] listFiles(File dir, FileFilter filter) throws FsException {
    File[] result = isDirectory(checkWriteable(dir)).listFiles(filter);
    if (result == null) {
      throw new FsException("problem listing files in [" + dir + "]");
    }
    return result;
  }
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
   * @param newFile the new name for the file
   * @throws FsFilenameExistsException if newFile exists
   * @throws FsException wrapping any underlying Exception that may occur
   */
  default void rename(File oldFile, File newFile) throws FsException {
    if (!checkWriteable(oldFile).renameTo(checkNonExistent(newFile))) {
      throw new FsException("problem renaming file [" + oldFile + "] to [" + newFile + "]");
    }
  }

  /**
   * <p>
   * Deletes the specified <code>file</code>. If the file doesn't exist returns quietly.
   * </p>
   *
   * @param file the file to delete in the
   * @throws FsException wrapping any underlying Exception that may occur
   */
  default void delete(File file) throws FsException {
    try {
      if (!checkWriteable(file).delete()) {
        throw new FsException("problem deleting file [" + file + "]");
      }
    }
    catch (FsFileNotFoundException e) {
    }
  }

  /**
   * <p>
   * Returns true if the passed <code>File</code> is a writeable directory, otherwise false.
   * </p>
   *
   * @param dir the directory to check in the
   * @return true if the passed <code>File</code> is a writeable directory, otherwise false
   * @throws FsException wrapping any underlying Exception that may occur
   */

  default boolean isWriteableDir(File dir) throws FsException {
    try {
      isDirectory(checkWriteable(dir));
      return true;
    }
    catch (FsException e) {
      return false;
    }
  }
  
  /** Throw an exception if the file does not exist.
   * 
   * @throws FsException if the file does not exist
   */
  static File checkExists(File file) throws FsException {
    Args.notNull(file, "file");
    if (!file.exists()) {
      throw new FsFileNotFoundException("Does not exist [" + file + "]");
    }
    return file;
  }

  /** Throw an exception if the file exists.
   * 
   * @throws FsException if the file exists
   */
  static File checkNonExistent(File file) throws FsException {
    Args.notNull(file,  "file");
    if (file.exists()) {
      throw new FsFilenameExistsException("Already exists [" + file + "]");
    }
    return file;
  }

  /** Throw an exception if the file is not readable
   * 
   */  
  static File checkReadable(File file) throws FsException {
    if (!checkExists(file).canRead()) {
      throw new FsException("Not readable [" + file + "]");
    }

    return file;
  }
  
  /** Throw an exception if the file is not readable or writeable
   * 
   */
  static File checkWriteable(File file) throws FsException {
    if (!checkReadable(file).canWrite()) {
      throw new FsException("Not writeable [" + file + "]");
    }
    return file;
  }
  
 
  /** Throw an exception if the file is not a directory
   * 
   */
  static File isDirectory(File file) throws FsException {
    if (!checkExists(file).isDirectory()) {
      throw new FsException("Not a directory [" + file + "]");
    }
    return file;
  }
  
  /** Throw an exception if the file is not a plain file.
   * 
   */ 
  static File isFile(File file) throws FsException {
    if (!checkExists(file).isFile()) {
      throw new FsException("Not a file [" + file + "]");
    }
    return file;
  }


}
