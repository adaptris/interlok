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

package com.adaptris.core.common;

import java.io.IOException;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@code DataInputParameter} implementation that reads from a file.
 * 
 * @config file-data-input-parameter
 *
 */
@XStreamAlias("file-data-input-parameter")
@DisplayOrder(order = {"url"})
public class FileDataInputParameter extends FileInputParameterImpl {

  @NotBlank
  private String url;

  public FileDataInputParameter() {

  }

  @Override
  public String extract(InterlokMessage message) throws CoreException {
    try {
      return this.load(new URLString(this.getUrl()), message.getContentEncoding());
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = Args.notBlank(url, "url");
  }


}
