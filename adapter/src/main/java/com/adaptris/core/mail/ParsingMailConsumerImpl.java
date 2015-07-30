package com.adaptris.core.mail;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.util.text.mime.PartSelector;

/**
 * Abstract base class for mail consumer implementations that will parse the MimeMessage.
 * 
 * @author dsefton
 * 
 */
public abstract class ParsingMailConsumerImpl extends MailConsumerImp {
  @NotNull
  @Valid
  private PartSelector partSelector;

  /**
   * Get the PartSelector.
   *
   * The PartSelector is used to select the part of the mail message required for converting to an AdaptrisMessage.
   *
   * @return Adaptris PartSelector object
   */
  public PartSelector getPartSelector() {
    return partSelector;
  }

  /**
   * Set the PartSelector.
   *
   * The PartSelector is used to select the part of the mail message required for converting to an AdaptrisMessage.
   *
   * @param ps Adaptris PartSelector object
   */
  public void setPartSelector(PartSelector ps) {
    partSelector = ps;
  }

}
