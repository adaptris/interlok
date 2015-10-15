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

package com.adaptris.core.lms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.adaptris.core.lms.ZipFileBackedMessageFactory.CompressionMode;
import com.adaptris.util.IdGenerator;

class ZipFileBackedMessageImpl extends FileBackedMessageImpl {

  private CompressionMode compressionMode;
  
  ZipFileBackedMessageImpl(IdGenerator guid, FileBackedMessageFactory fac, File tmpDir, int bufsiz, long maxSizeInline, CompressionMode compressionMode) {
    super(guid, fac, tmpDir, bufsiz, maxSizeInline);
    this.compressionMode = compressionMode;
  }

  /**
   * Return a stream with the contents of the first ZipEntry in the file
   */
  @Override
  public InputStream getInputStream() throws IOException {
    switch(compressionMode) {
    case Uncompress:
    case Both:
      if(inputFile != null) {
        // Check if the file is a Zip File since ZipInputStream doesn't error for non-zip files
        // This will throw ZipException for non-zip files (or very badly corrupted ones)
        try(ZipFile z = new ZipFile(inputFile)){};
      }
      
      ZipInputStream zin = new ZipInputStream(super.getInputStream());
      zin.getNextEntry();
      return zin;
      
    default:
      return super.getInputStream();
    }
  }
  
  /**
   * Return the size of the uncompressed data
   */
  @Override
  public long getSize() {
    // If the file size is 0 or if we have no file, no need to try and read it
    if(super.getSize() == 0) {
      return 0;
    }
    
    try(ZipFile zipFile = new ZipFile(inputFile)) {
      long size = 0;
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while(entries.hasMoreElements()) {
        size += entries.nextElement().getSize();
      }
      
      return size;
    } catch (ZipException e) {
      return 0;
    } catch (IOException e) {
      return 0;
    }
  }

  /**
   * Create a stream to write to the first entry in the zip file
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    switch(compressionMode) {
    case Compress:
    case Both:
      OutputStream out = super.getOutputStream();
      ZipOutputStream zout = new ZipOutputStream(out);
      zout.putNextEntry(new ZipEntry(getUniqueId()));
      return zout;
      
    default:
      return super.getOutputStream();
    }
  }

  @Override
  public void initialiseFrom(File sourceFile) throws IOException {
    try(ZipFile zipFile = new ZipFile(sourceFile)) {
      super.initialiseFrom(sourceFile);
    } catch (ZipException ze) {
      throw new UnsupportedOperationException(String.format(
          "\"%s\" is not a valid zip file. ZipFileBackedMessageFactory can only initialize from zip files. "
          + "For uncompressed files use FileBackedMessageFactory.", sourceFile.getAbsolutePath()));
    }
  }

}
