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

package com.adaptris.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;

@SuppressWarnings("deprecation")
public class ScriptingServiceTest extends GeneralServiceExample {

  private static final String KEY_SCRIPTING_BASEDIR = "scripting.basedir";
  public static final String SCRIPT = "\nvalue = message.getMetadataValue 'MyMetadataKey';"
      + "\nmessage.addMetadata('MyMetadataKey', value.reverse);";

  private static final String MY_METADATA_VALUE = "MyMetadataValue";
  private static final String MY_METADATA_KEY = "MyMetadataKey";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  private File writeScript(boolean working) throws IOException {
    FileWriter fw = null;
    File result = null;
    try {
      File dir = new File(PROPERTIES.getProperty(KEY_SCRIPTING_BASEDIR, "./build/scripting"));
      dir.mkdirs();
      result = File.createTempFile("junit", ".script", dir);
      fw = new FileWriter(result);
      fw.write(working ? SCRIPT : "This will fail");
    }
    finally {
      IOUtils.closeQuietly(fw);
    }
    return result;
  }

  private void delete(File script) throws Exception {
    if (script == null) {
      return;
    }
    if (!script.delete()) {
      throw new Exception("failed to delete file");
    }
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(MY_METADATA_KEY, MY_METADATA_VALUE);
    ScriptingService service = createService();
    File script = writeScript(true);
    service.setScriptFilename(script.getCanonicalPath());
    execute(service, msg);
    assertTrue(msg.containsKey(MY_METADATA_KEY));
    assertNotSame(MY_METADATA_VALUE, msg.getMetadataValue(MY_METADATA_KEY));
    assertEquals(new StringBuffer(MY_METADATA_VALUE).reverse().toString(), msg.getMetadataValue(MY_METADATA_KEY));
    delete(script);
  }

  @Test
  public void testInit() throws Exception {
    ScriptingService service = new ScriptingService();
    try {
      service.init();
      fail("Service initialised w/o a language");
    }
    catch (Exception expected) {
      ;
    }
    service.setLanguage("jruby");
    service.setScriptFilename("/BLAHBLAHBLAHBLAHBLAH/BLAHBLAHBLAHBLAH");
    try {
      service.init();
      fail("Service initialised with no idiotic filename");
    }
    catch (Exception expected) {
      ;
    }
    File script = writeScript(false);
    service.setScriptFilename(script.getCanonicalPath());
    LifecycleHelper.init(service);
    LifecycleHelper.close(service);
    delete(script);
  }

  @Test
  public void testDoServiceWithFailingScript() throws Exception {
    ScriptingService service = createService();
    File script = writeScript(false);
    service.setScriptFilename(script.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      execute(service, msg);
      fail("Service failure expected");
    }
    catch (ServiceException expected) {

    }
    delete(script);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ScriptingService service = createService();
    service.setScriptFilename("/path/to/script/you/want/to/execute");
    return service;
  }

  private ScriptingService createService() {
    ScriptingService result = new ScriptingService("scripting-service");
    result.setLanguage("jruby");
    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--"
        + "\nThis allows to embed scripts written in any language that supports JSR223 (e.g. jruby)."
        + "\nThe script is executed and the AdaptrisMessage that is due to be processed is"
        + "\nbound against the key 'message' and an instance of org.slf4j.Logger is also bound "
        + "\nto key 'log'. These can be used as a standard variable within the script." + "\n-->\n";
  }
}
