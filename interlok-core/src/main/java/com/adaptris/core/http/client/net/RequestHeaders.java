package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.http.client.RequestHeaderProvider;

import lombok.Getter;
import lombok.Setter;

public abstract class RequestHeaders implements RequestHeaderProvider<HttpURLConnection> {

  /**
   * Unfold headers onto a single line.
   * <p>
   * RFC7230 deprecates the folding of headers onto multiple lines; so HTTP headers are expected to be single line. This param allows you to
   * enforce that unfolding metadata values happens before writing them as request properties.
   * </p>
   *
   * @param unfold
   *          true to unfold values (default is false to preserve legacy behaviour).
   * @return true if set to unfold values.
   */
  @Getter
  @Setter
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean unfold;

  boolean unfold() {
    return BooleanUtils.toBooleanDefaultIfNull(getUnfold(), false);
  }

  String unfold(String s) {
    if (unfold()) {
      return s.replaceAll("\\s\\r\\n\\s+", " ").replaceAll("\\r\\n\\s+", " ");
    }
    return s;
  }

}
