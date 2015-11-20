/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

/**
 * <p>
 * Base class for unit tests which produce example XML config.
 * </p>
 */
public abstract class ExampleConfigCase extends BaseCase {

  // protected AdaptrisMarshaller xmlMarshaller;
  protected AdaptrisMarshaller configMarshaller;
  protected AdaptrisMarshaller defaultMarshaller;

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "SampleConfigCase.baseDir";

  private String baseDir;

  public ExampleConfigCase(String name) {
    super(name);
    try {
      defaultMarshaller = DefaultMarshaller.getDefaultMarshaller();
      // by default we want to marshal config w/o ID Referencing mode to remove "ID cruft".
      // So we aren't going to use the DefaultMarshaller *as is*
      configMarshaller = new XpathModeXStream();
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testCreateExampleConfig() throws Exception {
    if (baseDir != null) {
      List objects = retrieveObjectsForSampleConfig();
      for (Object object : objects) {
        File file = new File(baseDir + File.separator + createBaseFileName(object) + ".xml");
        if (file.exists()) {
          file.delete();
        }
        String xml = createExampleXml(object);
        xml = decorate(xml);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.write(xml.getBytes());
        raf.close();
      }
    }
  }

  protected String decorate(String xml) {
    String result = xml.replaceAll("  ", " ");
    // remove namespace clutter
    result = result.replaceAll(" xmlns:xsi=\"http://www\\.w3\\.org/2001/XMLSchema-instance\"", "");
    // remove empty unique-id tag clutter?
    result = result.replaceAll("\\s+<unique-id></unique-id>", "");
    return result;
  }

  public final void testXmlRoundTrip() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      Object unmarshalled = roundTrip(input, defaultMarshaller);
      doJavaxValidation(input, unmarshalled);
      assertRoundtripEquality(input, unmarshalled);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        Object unmarshalled = roundTrip(o, defaultMarshaller);
        doJavaxValidation(o, unmarshalled);
        assertRoundtripEquality(o, unmarshalled);
      }
    }
  }


  private Object roundTrip(Object input, AdaptrisMarshaller m) throws Exception {
    String xml = m.marshal(input);
    log.trace("Marshalled XML " + xml);
    return m.unmarshal(xml);
  }

  /**
   * Retrieve the object for testing castor round tripping.
   * <p>
   * This seems like a hack, but it is intentional. In some cases for retrieveObjectForSampleConfig() cases we are setting values to
   * null, which when round-tripped revert back to the default, forcing the test to fail.
   * </p>
   * Sub-classes may override this.
   *
   * @return the object for castor round tripping.
   */
  protected Object retrieveObjectForCastorRoundTrip() {
    return retrieveObjectForSampleConfig();
  }

  protected String createBaseFileName(Object object) {
    return object.getClass().getName();
  }

  /**
   * <p>
   * This method may optionally be over-ridden by sub-classes which require to create sample config for more than one class.
   * </p>
   */
  protected List retrieveObjectsForSampleConfig() {
    List result = new ArrayList();
    result.add(retrieveObjectForSampleConfig());

    return result;
  }

  protected abstract String createExampleXml(Object object) throws Exception;

  protected abstract Object retrieveObjectForSampleConfig();

  protected String getExampleCommentHeader(Object object) {
    return "<!--\n\n  Please refer to Javadocs for additional config " + "information.\n\n-->\n";
  }

  protected String getBaseDir() {
    return baseDir;
  }

  protected void setBaseDir(String s) {
    baseDir = s;

    if (baseDir != null) {
      File dir = new File(baseDir);
      dir.mkdirs();
    }
  }

  protected AdaptrisMarshaller createMarshaller() {
    return defaultMarshaller;
  }

  private class XpathModeXStream extends XStreamMarshaller {
    public XpathModeXStream() throws CoreException {
      super();
    }

    @Override
    public String marshal(Object obj) throws CoreException {
      XStream xstream = AdapterXStreamMarshallerFactory.getInstance().createXStream();
//      XStream xstream = XStreamBootstrap.createXStream();
//      xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
      String xmlResult = xstream.toXML(obj);
      return xmlResult;
    }

    @Override
    public void marshal(Object obj, Writer writer) throws CoreException {
      try {
        XStream xstream = AdapterXStreamMarshallerFactory.getInstance().createXStream();
//        XStream xstream = XStreamBootstrap.createXStream();
//        xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
        xstream.toXML(obj, writer);
        writer.flush();
      }
      catch (Exception ex) {
        throw new CoreException(ex);
      }
    }
  }
}
