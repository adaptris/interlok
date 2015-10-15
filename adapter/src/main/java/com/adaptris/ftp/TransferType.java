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

package com.adaptris.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Represents the FTP Transfer type.
 * 
 * 
 */
public enum TransferType {

  BINARY(FTPClient.BINARY_FILE_TYPE), ASCII(FTPClient.ASCII_FILE_TYPE);
  private int fileType;

  TransferType(int type) {
    fileType = type;
  }

  public void applyTransferType(FTPClient ftp) throws IOException {
    ftp.setFileType(fileType);
  }
}
