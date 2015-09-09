package com.adaptris.core.services.routing;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
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

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() {
    // Nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // Nothing
  }
}
