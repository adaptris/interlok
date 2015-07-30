package com.adaptris.core.http.jetty;

import javax.servlet.Servlet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jetty.servlet.ServletHolder;

class ServletWrapper {
  private ServletHolder servletHolder;
  private String url;

  private ServletWrapper() {

  }

  ServletWrapper(Servlet s, String urlMapping) {
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
