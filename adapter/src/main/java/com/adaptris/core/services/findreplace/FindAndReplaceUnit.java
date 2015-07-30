package com.adaptris.core.services.findreplace;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A unit of configuration for doing find and replace.
 * 
 * @config find-and-replace-unit
 * @author lchan
 * 
 */
@XStreamAlias("find-and-replace-unit")
public class FindAndReplaceUnit {

  private ReplacementSource find;
  private ReplacementSource replace;
  
  public FindAndReplaceUnit() {
    
  }

  public FindAndReplaceUnit(ReplacementSource source, ReplacementSource dest) {
    this();
    setFind(source);
    setReplace(dest);
  }

  public ReplacementSource getFind() {
    return find;
  }

  public void setFind(ReplacementSource find) {
    this.find = find;
  }

  public ReplacementSource getReplace() {
    return replace;
  }

  public void setReplace(ReplacementSource replace) {
    this.replace = replace;
  }
}
