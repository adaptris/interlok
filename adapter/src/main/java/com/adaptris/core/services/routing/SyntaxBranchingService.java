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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Branching Service which determines the next Service to apply according to <code>SyntaxIdentifier</code>s, as used by
 * <code>SyntaxRoutingService</code>.
 * </p>
 * 
 * @config syntax-branching-service
 * 
 * @license STANDARD
 * @see SyntaxIdentifier
 * @see SyntaxRoutingService
 */
@XStreamAlias("syntax-branching-service")
public class SyntaxBranchingService extends BranchingServiceImp {

  @NotNull
  @AutoPopulated
  @XStreamImplicit
  private List<SyntaxIdentifier> syntaxIdentifiers = new ArrayList<SyntaxIdentifier>();

  /**
   * @see com.adaptris.core.Service
   *      #doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String message = msg.getStringPayload();
    String destination = null;
    for (SyntaxIdentifier ident : syntaxIdentifiers) {
      if (ident.isThisSyntax(message)) {
        destination = ident.getDestination();
        break;
      }
    }
    if (destination == null) {
      throw new ServiceException("Unable to identify the message syntax to branch on");
    }

    msg.setNextServiceId(destination);
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

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {}

}
