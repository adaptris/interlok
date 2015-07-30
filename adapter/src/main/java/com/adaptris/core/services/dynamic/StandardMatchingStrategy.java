package com.adaptris.core.services.dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * <code>StandardMatchingStrategy</code> creates a <code>TradingRelationship[]</code> which substitutes
 * <code>TradingRelationship.WILD_CARD</code>s in the progressively more generic order described below.
 * <ul>
 * <li>source</li>
 * <li>destination</li>
 * <li>type</li>
 * <li>source & destination</li>
 * <li>destination & type</li>
 * <li>source & type</li>
 * <li>source, destinatio & type</li>
 * </ul>
 * </p>
 * <p>
 * Care should be taken if the passed <code>TradingRelationship</code> already contains wildcards.
 * </p>
 * 
 * @config standard-matching-strategy
 */
@XStreamAlias("standard-matching-strategy")
public class StandardMatchingStrategy implements MatchingStrategy {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * <p>
   * NB exisitng wildcards...
   * </p>
   * @see com.adaptris.core.services.dynamic.MatchingStrategy
   *   #create(com.adaptris.core.TradingRelationship)
   */
  @Override
  public TradingRelationship[] create(TradingRelationship t)
    throws CoreException {

    TradingRelationship[] result = null;

    if (t == null) {
      result = new TradingRelationship[0];
    }
    else {
      result = new TradingRelationship[8];
      result[0] = t;

      try {
        TradingRelationship copy1 = (TradingRelationship) t.clone();
        copy1.setSource(TradingRelationship.WILD_CARD);
        result[1] = copy1;

        TradingRelationship copy2 = (TradingRelationship) t.clone();
        copy2.setDestination(TradingRelationship.WILD_CARD);
        result[2] = copy2;

        TradingRelationship copy3 = (TradingRelationship) t.clone();
        copy3.setType(TradingRelationship.WILD_CARD);
        result[3] = copy3;

        TradingRelationship copy4 = (TradingRelationship) t.clone();
        copy4.setSource(TradingRelationship.WILD_CARD);
        copy4.setDestination(TradingRelationship.WILD_CARD);
        result[4] = copy4;

        TradingRelationship copy5 = (TradingRelationship) t.clone();
        copy5.setDestination(TradingRelationship.WILD_CARD);
        copy5.setType(TradingRelationship.WILD_CARD);
        result[5] = copy5;

        TradingRelationship copy6 = (TradingRelationship) t.clone();
        copy6.setSource(TradingRelationship.WILD_CARD);
        copy6.setType(TradingRelationship.WILD_CARD);
        result[6] = copy6;

        TradingRelationship copy7 = (TradingRelationship) t.clone();
        copy7.setSource(TradingRelationship.WILD_CARD);
        copy7.setDestination(TradingRelationship.WILD_CARD);
        copy7.setType(TradingRelationship.WILD_CARD);
        result[7] = copy7;
      }
      catch (CloneNotSupportedException e) {
        throw new CoreException(e);
      }
    }

//    log.debug(result);

    return result;
  }
}
