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

package com.adaptris.core.services.routing;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Extracts data from an AdaptrisMessage and stores it against metadata.
 * <p>
 * This is somewhat similar to the available MetadataService, however it uses a list of <code>SyntaxIdentifiers</code> in order to
 * determine the value that should be stored against a particular metadata key.
 * </p>
 * <p>
 * Each <code>SyntaxIdentifier</code> is tried in turn, until <b>true</b> is returned by the method
 * <code>isThisSyntax(AdaptrisMessage)</code>. At this point, the value returned by <code>getDestination()</code> is stored against
 * the configured key.
 * </p>
 * 
 * @config syntax-routing-service
 * 
 * @author sellidge
 */
@XStreamAlias("syntax-routing-service")
@AdapterComponent
@ComponentProfile(summary = "Identify a message, and set a metadata key based on the identifier", tag = "service,routing")
@DisplayOrder(order = {"routingKey", "syntaxIdentifiers"})
public class SyntaxRoutingService extends ServiceImp {
  private String routingKey = null;
  @XStreamImplicit
  @Valid
  @NotNull
  @AutoPopulated
  private List<SyntaxIdentifier> syntaxIdentifiers = new ArrayList<SyntaxIdentifier>();

  public SyntaxRoutingService() {

  }
  /**
   * @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String message = msg.getContent();
    String destination = null;

    for (int i = 0; i < syntaxIdentifiers.size(); i++) {
      SyntaxIdentifier ident = syntaxIdentifiers.get(i);

      if (ident.isThisSyntax(message)) {
        destination = ident.getDestination();
        break;
      }
    }

    if (destination == null) {
      throw new ServiceException("Unable to identify the message syntax for routing");
    }

    msg.addMetadata(routingKey, destination);
    return;
  }

  /**
   * Add a SyntaxIdentifier to the configured list.
   *
   * @param ident the SyntaxIdentifier.
   */
  public void addSyntaxIdentifier(SyntaxIdentifier ident) {
    if (ident == null) {
      throw new IllegalArgumentException("Identifier is null");
    }

    syntaxIdentifiers.add(ident);
  }

  /**
   * Return the list of configured SyntaxIdentifers.
   *
   * @return the list.
   */
  public List<SyntaxIdentifier> getSyntaxIdentifiers() {
    return syntaxIdentifiers;
  }

  /**
   * Sets the list of configured SyntaxIdentifers.
   *
   * @param l the list.
   */
  public void setSyntaxIdentifiers(List<SyntaxIdentifier> l) {
    if (l == null) {
      throw new IllegalArgumentException("List is null");
    }
    syntaxIdentifiers = l;
  }

  /**
   * Set the metadata key that the value will be stored against.
   *
   * @param key the key.
   */
  public void setRoutingKey(String key) {
    if (isBlank(key)) {
      throw new IllegalArgumentException("Null routing Key");
    }
    routingKey = key;
  }

  /**
   * Get the metadata key that the value will be stored against.
   *
   * @return the key.
   */
  public String getRoutingKey() {
    return routingKey;
  }

  @Override
  protected void initService() throws CoreException {
    if (isBlank(routingKey)) {
      throw new CoreException("No Routing Key defined");
    }
  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {
  }


}
