package com.adaptris.core.http.jetty.retry;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.util.text.mime.MimeConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * Supports reporting of what's in the retry store.
 * <p>
 * This is tightly coupled with {@link RetryFromJetty} and probably can't be used elsewhere.
 * </p>
 */
@XStreamAlias("jetty-retry-report-builder")
@ComponentProfile(summary = "Generate a report on the files stored in the retry store.",
    since = "3.11.1")
@DisplayOrder(order = {"reportRenderer", "contentType"})
public class ReportBuilder implements ComponentLifecycle {

  @Getter
  @Setter
  @InputFieldDefault(value = "just the names newline separated")
  private BlobListRenderer reportRenderer;

  /**
   * Set the content type to be associated with the report.
   *
   */
  @Getter
  @Setter
  @InputFieldDefault(value = MimeConstants.CONTENT_TYPE_TEXT_PLAIN)
  private String contentType;

  public AdaptrisMessage build(Iterable<RemoteBlob> list, AdaptrisMessage msg) throws Exception {
    renderer().render(list, msg);
    msg.addMessageHeader(RetryFromJetty.CONTENT_TYPE_METADATA_KEY, contentType());
    return msg;
  }

  private BlobListRenderer renderer() {
    return ObjectUtils.defaultIfNull(getReportRenderer(), new BlobListRenderer() {});
  }

  private String contentType() {
    return StringUtils.defaultIfBlank(getContentType(), MimeConstants.CONTENT_TYPE_TEXT_PLAIN);
  }

}