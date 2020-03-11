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

package com.adaptris.core.services;

import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.EncodingHelper.Base64Encoding;

public abstract class Base64Service extends ServiceImp {

  @InputFieldDefault(value = "MIME")
  private Base64Encoding style = null;

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
  }

  public Base64Encoding getStyle() {
    return style;
  }

  public void setStyle(Base64Encoding style) {
    this.style = style;
  }

  protected Base64Encoding style() {
    return ObjectUtils.defaultIfNull(getStyle(), Base64Encoding.MIME);
  }

  public <T extends Base64Service> T withStyle(Base64Encoding style) {
    setStyle(style);
    return (T) this;
  }

}
