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

/**
 *  Encapsulates the FTP server reply
 *
 *  @author      Bruce Blackshaw
 */
public final class Reply {

  /**
   *  Reply code
   */
  private String replyCode;

  /**
   *  Reply text
   */
  private String replyText;

  /**
   *  Constructor. Only to be constructed
   *  by this package, hence package access
   *
   *  @param  replyCode  the server's reply code
   *  @param  replyText  the server's reply text
   */
  Reply(String replyCode, String replyText) {
    this.replyCode = replyCode;
    this.replyText = replyText;
  }

  /**
   *  Getter for reply code
   *
   *  @return server's reply code
   */
  public String getReplyCode() {
    return replyCode;
  }

  /**
   *  Getter for reply text
   *
   *  @return server's reply text
   */
  public String getReplyText() {
    return replyText;
  }

}
