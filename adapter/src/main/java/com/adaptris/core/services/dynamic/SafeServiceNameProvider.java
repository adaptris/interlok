package com.adaptris.core.services.dynamic;

import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extension of <code>DefaultServiceNameProvider</code> which strips the following characters from any component of the Trading
 * Relationship:
 * <p>
 * <code>/,\,?,*,:,|, ,&,",&lt;,&gt;,'</code>
 * </p>
 * Of particular use for ebXML where it is feasible that URLs might be used to identify the parties or message type.
 * 
 * @config safe-service-name-provider
 * 
 * @author Stuart Ellidge
 */
@XStreamAlias("safe-service-name-provider")
public final class SafeServiceNameProvider extends DefaultServiceNameProvider {
  public SafeServiceNameProvider() {
	super();
  }

  @Override
  protected String retrieveName(TradingRelationship t) throws CoreException {
    if (t == null) {
      throw new IllegalArgumentException("null param");
    }

    String name = super.retrieveName(t);
    name = name.replaceAll("\\/", "");
    name = name.replaceAll("\\\\", "");
    name = name.replaceAll("\\?", "");
    name = name.replaceAll("\\*", "");
    name = name.replaceAll("\\:", "");
    name = name.replaceAll(" ", "");
    name = name.replaceAll("\\|", "");
    name = name.replaceAll("&", "");
    name = name.replaceAll("\\\"", "");
    name = name.replaceAll("\\'", "");
    name = name.replaceAll("<", "");
    name = name.replaceAll(">", "");
    return name;
  }
}
