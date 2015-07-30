package com.adaptris.core.transform;

import java.util.HashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.util.XmlHelper;

public class TransformParameterCase extends BaseCase {

  protected static final String XML_DOC = "<document>data</document>";
  protected static final String KEY_STRING_METADATA = "myStringMetadata";
  protected static final String KEY_STRING_METADATA_2 = "anotherStringMetadata";
  protected static final String KEY_OBJECT_METADATA = "myObjectMetadata";
  protected static final String KEY_OBJECT_METADATA_2 = "anotherObjectMetadata";

  protected static final String METADATA_VALUE = "myStringMetadataValue";

  public TransformParameterCase(String name) {
    super(name);
  }

  public void testIgnoreMetadataParameter() throws Exception {
    IgnoreMetadataParameter p = new IgnoreMetadataParameter();
    Map existingParams = new HashMap();
    AdaptrisMessage msg = createMessage();
    assertNull(p.createParameters(msg, existingParams));
  }

  protected AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_STRING_METADATA, METADATA_VALUE);
    msg.addMetadata(KEY_STRING_METADATA_2, "another value");
    msg.getObjectMetadata().put(KEY_OBJECT_METADATA, XmlHelper.createDocument(XML_DOC));
    msg.getObjectMetadata().put(KEY_OBJECT_METADATA_2, new Object());
    return msg;
  }
}
