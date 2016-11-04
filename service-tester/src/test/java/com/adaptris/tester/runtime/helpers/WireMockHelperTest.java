package com.adaptris.tester.runtime.helpers;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WireMockHelperTest extends HelperCase {

  public WireMockHelperTest(String name) {
    super(name);
  }

  public void testGet() throws Exception{
    final String serviceFile = "http_stubs";
    File testFile = new File(this.getClass().getClassLoader().getResource(serviceFile).getFile());

    WireMockHelper wireMockHelper = new WireMockHelper();
    DynamicPortProvider portProvider = new DynamicPortProvider(8080);
    wireMockHelper.setPortProvider(portProvider);
    wireMockHelper.setFileSource(testFile.getAbsolutePath());
    wireMockHelper.init();

    URL url = new URL("http://localhost:" + portProvider.getPort() + "/hello" );
    URLConnection urlConnection = url.openConnection();
    BufferedReader in = new BufferedReader(
        new InputStreamReader(
            urlConnection.getInputStream()));
    String inputLine;

    StringBuilder response = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    assertEquals("{\"hello\": \"world\"}",response.toString());
    assertTrue(wireMockHelper.getHelperProperties().containsKey("wire.mock.helper.port"));
    assertTrue(8080 <= Integer.valueOf(wireMockHelper.getHelperProperties().get("wire.mock.helper.port")));
    in.close();
    wireMockHelper.close();
  }

  @Override
  protected Helper createHelper() {
    WireMockHelper wireMockHelper = new WireMockHelper();
    DynamicPortProvider portProvider = new DynamicPortProvider(8080);
    wireMockHelper.setPortProvider(portProvider);
    wireMockHelper.setFileSource("/home/user/http_stubs");
    return wireMockHelper;
  }
}