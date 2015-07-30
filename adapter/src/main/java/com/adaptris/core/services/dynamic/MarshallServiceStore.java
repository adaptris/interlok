package com.adaptris.core.services.dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;

/**
 * <p>
 * Implementation of {@link ServiceStore} which uses an XML marshaller for Services.
 * </p>
 */
public abstract class MarshallServiceStore implements ServiceStore {

  private AdaptrisMarshaller marshaller;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Default constructor.
   * <ul>
   * <li>Default imp. class is <code>com.adaptris.core.ServiceList</code></li>
   * <ul>
   *
   * @throws CoreException
   */
  public MarshallServiceStore() throws CoreException {
  }

  /**
   * Gets the marshalling implementation.
   *
   * @return {@link AdaptrisMarshaller}
   */
  public AdaptrisMarshaller getMarshaller() {
    return marshaller;
  }

  /**
   * Sets the marshalling implementation.
   *
   * @param marshaller
   */
  public void setMarshaller(AdaptrisMarshaller marshaller) {
    this.marshaller = marshaller;
  }

  protected AdaptrisMarshaller currentMarshaller() throws CoreException {
    return getMarshaller() != null ? getMarshaller() : DefaultMarshaller.getDefaultMarshaller();
  }
}
