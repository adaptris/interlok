package com.adaptris.core.services.jdbc;



/**
 * Abstract class for translating result sets into metadata.
 *
 * @author lchan
 *
 */
public abstract class MetadataResultSetTranslatorImpl extends ResultSetTranslatorImp {

  protected static final String DEFAULT_METADATA_KEY = "JdbcDataQueryServiceOutput";

  private String metadataKeyPrefix;
  private String separator;
  private String resultSetCounterPrefix;

  public MetadataResultSetTranslatorImpl() {
    super();
    setMetadataKeyPrefix(DEFAULT_METADATA_KEY);
    setSeparator("_");
  }

  public String getMetadataKeyPrefix() {
    return metadataKeyPrefix;
  }

  /**
   * Set the metadata key prefix for each metadata key generated.
   *
   * @param s
   */
  public void setMetadataKeyPrefix(String s) {
    metadataKeyPrefix = s;
  }

  public String getSeparator() {
    return separator;
  }

  /**
   * Set the separator between the prefix and the generated column name.
   *
   * @param s
   */
  public void setSeparator(String s) {
    separator = s;
  }

  public String getResultSetCounterPrefix() {
    return resultSetCounterPrefix;
  }

  public void setResultSetCounterPrefix(String resultSetCounterPrefix) {
    this.resultSetCounterPrefix = resultSetCounterPrefix;
  }
}
