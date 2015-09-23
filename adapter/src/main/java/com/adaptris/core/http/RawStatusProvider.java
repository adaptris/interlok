package com.adaptris.core.http;

import java.net.HttpURLConnection;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Static implementation of {@link HttpStatusProvider} that allows a numeric status code.
 *
 * @config http-raw-status
 * @author lchan
 *
 */
@XStreamAlias("http-raw-status")
public class RawStatusProvider implements HttpStatusProvider {

  private int code;
  @AdvancedConfig
  private String text;

  public RawStatusProvider() {
    setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
  }

  public RawStatusProvider(int status) {
    this();
    setCode(status);
  }

  @Override
  public Status getStatus(AdaptrisMessage msg) {
    return new HttpStatusBuilder().withCode(getCode()).withText(getText()).build();
  }


  public int getCode() {
    return code;
  }


  /**
   * Set the HTTP Status to use.
   * 
   * @param status the status, which defaults to {@value java.net.HttpURLConnection#HTTP_INTERNAL_ERROR}
   */
  public void setCode(int status) {
    this.code = status;
  }


  public String getText() {
    return text;
  }


  /**
   * Set the optional response text that will be sent with the response code.
   * 
   * <p>Note that for {@link com.adaptris.core.http.jetty.ResponseProducer} any values configured here will be ignored as
   * that will use {@link javax.servlet.http.HttpServletResponse#setStatus(int)} method only. This field is included only for
   * completeness, a sensible default will be made available based on the {@link HttpStatus} selected.
   * </p>
   * 
   * @param statusText the text to be sent (e.g. {@code OK}, if the status code is {@value HttpURLConnection#HTTP_OK}).
   */
  public void setText(String txt) {
    this.text = txt;
  }

}
