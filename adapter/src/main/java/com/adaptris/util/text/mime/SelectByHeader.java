package com.adaptris.util.text.mime;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * {@link PartSelector} implementation that parses a specific header examining the value to see if it matches the configured regular
 * expression.
 * 
 * @config mime-select-by-header
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mime-select-by-header")
public class SelectByHeader implements PartSelector {

  @NotNull
  @NotBlank
  private String headerName;
  @NotNull
  @NotBlank
  private String headerValueRegExp;

  public SelectByHeader() {
  }

  public SelectByHeader(String header, String regexp) {
    this();
    setHeaderName(header);
    setHeaderValueRegExp(regexp);
  }

  /**
   * Selects the first matching MimeBodyPart from the MultiPartInput.
   *
   * @see PartSelector#select(MultiPartInput)
   */
  @Override
  public MimeBodyPart select(MultiPartInput m) throws MessagingException {
    MimeBodyPart result = null;
    assertConfig();
    outer: while (m.hasNext()) {
      MimeBodyPart p = (MimeBodyPart) m.next();
      String[] values = p.getHeader(getHeaderName());
      if (values != null) {
        for (String value : values) {
          if (value.matches(getHeaderValueRegExp())) {
            result = p;
            break outer;
          }
        }
      }
    }
    return result;
  }

  private void assertConfig() throws MessagingException {
    if (getHeaderName() == null || "".equals(getHeaderName())) {
      throw new MessagingException("No configured Header name");
    }
    if (getHeaderValueRegExp() == null || "".equals(getHeaderValueRegExp())) {
      throw new MessagingException(
          "No configured Header value regular expression");
    }
  }

  @Override
  /**
   * Functionality not supported in this PartSelector
   */
  public List<MimeBodyPart> select(MimeMultipart in) throws MessagingException {
  	throw new MessagingException("Functionality not currently supported in this PartSelector");
  }

  /**
   * @return the headerName
   */
  public String getHeaderName() {
    return headerName;
  }

  /**
   * Specify the header name whose value will be examined .
   *
   * @param s the headerName to set, e.g. "Content-Type"
   */
  public void setHeaderName(String s) {
    headerName = s;
  }

  /**
   * @return the headerValueRegExp
   */
  public String getHeaderValueRegExp() {
    return headerValueRegExp;
  }

  /**
   * Set the value of the regular expression that will be used to match against
   * the header value.
   *
   * @param s the headerValueRegExp to set
   */
  public void setHeaderValueRegExp(String s) {
    headerValueRegExp = s;
  }
}
