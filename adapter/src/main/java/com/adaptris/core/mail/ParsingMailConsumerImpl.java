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

package com.adaptris.core.mail;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.util.text.mime.PartSelector;

/**
 * Abstract base class for mail consumer implementations that will parse the MimeMessage.
 * 
 * @author dsefton
 * 
 */
public abstract class ParsingMailConsumerImpl extends MailConsumerImp {
  @NotNull
  @Valid
  private PartSelector partSelector;

  /**
   * Get the PartSelector.
   *
   * The PartSelector is used to select the part of the mail message required for converting to an AdaptrisMessage.
   *
   * @return Adaptris PartSelector object
   */
  public PartSelector getPartSelector() {
    return partSelector;
  }

  /**
   * Set the PartSelector.
   *
   * The PartSelector is used to select the part of the mail message required for converting to an AdaptrisMessage.
   *
   * @param ps Adaptris PartSelector object
   */
  public void setPartSelector(PartSelector ps) {
    partSelector = ps;
  }

}
