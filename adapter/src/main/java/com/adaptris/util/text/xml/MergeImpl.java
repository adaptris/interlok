package com.adaptris.util.text.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merge implementation that replaces a node derived from an Xpath.
 *
 * @author lchan
 *
 */
abstract class MergeImpl implements DocumentMerge {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  public MergeImpl() {
  }

}
