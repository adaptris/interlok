package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>MessageSplitter</code> which allows a single <code>AdaptrisMessage</code> that contains a metadata key
 * that is considered to contain multiple elements to be split.
 * </p>
 * 
 * @config split-by-metadata
 * 
 * @author lchan
 */
@XStreamAlias("split-by-metadata")
public class SplitByMetadata extends MessageSplitterImp {
  @NotNull
  @AutoPopulated
  @NotBlank
  private String separator;
  @NotNull
  @NotBlank
  private String metadataKey;
  @NotNull
  @NotBlank
  private String splitMetadataKey;

  public SplitByMetadata() {
    super();
    setSeparator(",");
  }

  public SplitByMetadata(String metadataKey, String splitMetadataKey) {
    this();
    setMetadataKey(metadataKey);
    setSplitMetadataKey(splitMetadataKey);
  }

  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg)
      throws CoreException {

    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    String value = msg.getMetadataValue(getMetadataKey());
    if (value == null) {
      logR.warn(getMetadataKey() + " returned no data");
      result.add(msg);
    }
    else {
      try {
        StringTokenizer st = new StringTokenizer(value, getSeparator());
        AdaptrisMessageFactory fac = selectFactory(msg);
        while (st.hasMoreTokens()) {
          AdaptrisMessage m = fac.newMessage(msg.getPayload());
          copyMetadata(msg, m);
          m.addMetadata(getSplitMetadataKey(), st.nextToken());
          result.add(m);
        }
      }
      catch (Exception e) {
        throw new CoreException(e);
      }
    }
    logR.trace("Split on " + value + " gave " + result.size() + " messages");
    return result;
  }

  /**
   * @return the splitToken
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * @param splitToken the splitToken to set
   */
  public void setSeparator(String splitToken) {
    separator = splitToken;
  }

  /**
   * @return the metadataKey
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * @param s the metadataKey to derive splis from.
   */
  public void setMetadataKey(String s) {
    metadataKey = s;
  }

  /**
   * @return the splitMetadataKey
   */
  public String getSplitMetadataKey() {
    return splitMetadataKey;
  }

  /**
   * The metadata key where the split value from {@link #setMetadataKey(String)}
   * will be stored.
   * 
   * @param s the splitMetadataKey to set
   */
  public void setSplitMetadataKey(String s) {
    splitMetadataKey = s;
  }

}
