package com.adaptris.core.services.mime;

import java.io.ByteArrayOutputStream;

import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.metadata.MetadataTarget;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.PartSelector;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Select a mime part with a selector and add it to a metadata
 */
@XStreamAlias("part-selector-to-metadata")
@AdapterComponent
@DisplayOrder(order = { "selector", "metadataKey", "metadataTarget" })
public class PartSelectorToMetadata {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * The selector to select a mime part from the message
   */
  @NotNull
  @NonNull
  @Getter
  @Setter
  @Valid
  private PartSelector selector;

  /**
   * The metadata key to add the mime part to
   */
  @NotNull
  @NonNull
  @Getter
  @Setter
  private String metadataKey;

  /**
   * The metadata target type: {@link MetadataTarget#Standard} or {@link MetadataTarget#Object} depending if the metadata is a string or an
   * object.
   */
  @NotNull
  @NonNull
  @Getter
  @Setter
  @AutoPopulated
  @InputFieldDefault(value = "Standard")
  private MetadataTarget metadataTarget = MetadataTarget.Standard;

  public void apply(AdaptrisMessage msg) throws ServiceException {
    try {
      BodyPartIterator mp = MimeHelper.createBodyPartIterator(msg);
      MimeBodyPart part = getSelector().select(mp);
      if (part != null) {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        StreamUtil.copyAndClose(part.getInputStream(), bytesOut);
        getMetadataTarget().apply(msg, getMetadataKey(), bytesOut);
      } else {
        log.warn("Could not select a MimePart to add to metadata {}, ignoring", getMetadataKey());
      }
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

}
