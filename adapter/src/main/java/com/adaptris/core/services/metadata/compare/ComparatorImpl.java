package com.adaptris.core.services.metadata.compare;


public abstract class ComparatorImpl implements MetadataComparator {
  
  private String resultKey;
  
  public ComparatorImpl() {
    setResultKey(getClass().getCanonicalName());
  }

  public String getResultKey() {
    return resultKey;
  }

  /**
   * Set the key where we store the result.
   * 
   * @param rk the key, default is the classname
   */
  public void setResultKey(String rk) {
    this.resultKey = rk;
  }

}
