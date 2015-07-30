package com.adaptris.core.services.dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;

/**
 * <p>
 * Partial implementation of <code>ServiceNameProvider</code>.
 * </p>
 */
public abstract class ServiceNameProviderImp implements ServiceNameProvider {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * @see com.adaptris.core.services.dynamic.ServiceNameProvider
   *   #obtain(com.adaptris.core.TradingRelationship)
   */
  @Override
  public String obtain(TradingRelationship t) throws CoreException {
    if (t == null) {
      throw new IllegalArgumentException("null param");
    }

    TradingRelationship[] ts = new TradingRelationship[1];
    ts[0] = t;

    return this.obtain(ts);
  }

  /**
   * <p>
   * Delegates <code>retrieveName</code> to concrete implementations.
   * </p>
   * @see com.adaptris.core.services.dynamic.ServiceNameProvider
   *   #obtain(com.adaptris.core.TradingRelationship[])
   */
  @Override
  public String obtain(TradingRelationship[] matches) throws CoreException {
    if (matches == null) {
      throw new IllegalArgumentException("null param");
    }

    String result = null;

    for (int i = 0; i < matches.length; i++) {
      result = retrieveName(matches[i]);

      if (result != null) {
        log.debug
        ("service logical name [" + result + "] matched on " + matches[i]);

        break;
      }
    }
    return result;
  }

  protected abstract String retrieveName(TradingRelationship t)
    throws CoreException;
}
