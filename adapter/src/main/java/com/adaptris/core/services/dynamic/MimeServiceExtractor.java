package com.adaptris.core.services.dynamic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.text.mime.PartSelector;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ServiceExtractor} implementation that uses a {@link PartSelector} to extract where the service is.
 * 
 * @config dynamic-mime-service-extractor
 * @author lchan
 * 
 */
@XStreamAlias("dynamic-mime-service-extractor")
public class MimeServiceExtractor implements ServiceExtractor {

  @NotNull
  @Valid
  private PartSelector selector;

  public MimeServiceExtractor() {

  }

  public MimeServiceExtractor(PartSelector selector) {
    this();
    setSelector(selector);
  }

  @Override
  public InputStream getInputStream(AdaptrisMessage m) throws ServiceException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    InputStream in = null;
    if (selector == null) {
      throw new ServiceException("No Selector, impossible to extract service");
    }
    try {
      MimeBodyPart part = selector.select(MimeHelper.create(m, false));
      if (part != null) {
        out = new ByteArrayOutputStream();
        in = part.getInputStream();
        IOUtils.copy(in, out);
      }
      else {
        throw new ServiceException("Could not select a part");
      }
    }
    catch (MessagingException e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(in);
    }
    return new ByteArrayInputStream(out.toByteArray());
  }

  public PartSelector getSelector() {
    return selector;
  }

  /**
   * Set the {@link PartSelector} implementation to use.
   * 
   * @param selector the part selector.
   */
  public void setSelector(PartSelector selector) {
    this.selector = selector;
  }

}
