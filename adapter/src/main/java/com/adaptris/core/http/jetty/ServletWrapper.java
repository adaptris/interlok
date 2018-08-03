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

package com.adaptris.core.http.jetty;

import javax.servlet.Servlet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Wrapper class around a servlet for jetty.
 * 
 */
public class ServletWrapper {
  private ServletHolder servletHolder;
  private String url;

  protected ServletWrapper() {

  }

  public ServletWrapper(Servlet s, String urlMapping) {
    servletHolder = new ServletHolder(s);
    url = urlMapping;
  }

  public ServletHolder getServletHolder() {
    return servletHolder;
  }

  public String getUrl() {
    return url;
  }


  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (o instanceof ServletWrapper) {
      ServletWrapper rhs = (ServletWrapper) o;
      return new EqualsBuilder().append(getServletHolder(), rhs.getServletHolder()).append(getUrl(), rhs.getUrl()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 17).append(getServletHolder()).append(getUrl()).toHashCode();
  }
}
