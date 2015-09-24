package com.adaptris.core.http.client.net;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;

import com.adaptris.core.util.Args;

public abstract class RequestHeadersCase {

  @Rule
  public TestName testName = new TestName();


  protected static boolean contains(URLConnection request, String headerKey, String headerValue) {
    boolean matched = false;
    String compareKey = Args.notEmpty(headerKey, "key");
    String compareValue = Args.notEmpty(headerValue, "value");
    Map<String, List<String>> headers = request.getRequestProperties();
    for (String h : headers.keySet()) {
      if (h.equals(compareKey)) {
        List<String> values = headers.get(h);
        for (String v : values) {
          if (v.equals(compareValue)) {
            matched = true;
            break;
          }
        }
      }
    }
    return matched;
  }

}
