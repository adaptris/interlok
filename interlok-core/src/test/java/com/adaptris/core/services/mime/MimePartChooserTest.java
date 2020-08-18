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

package com.adaptris.core.services.mime;

import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.PART3_CONTENT_ID;
import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.PAYLOAD_1;
import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.PAYLOAD_2;
import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.PAYLOAD_3;
import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.create;
import static com.adaptris.util.text.mime.MimeConstants.HEADER_CONTENT_ENCODING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.mime.PartSelector;

@SuppressWarnings("deprecation")
public class MimePartChooserTest extends MimeServiceExample {

  private static final String PREFIX_PART_HDR = "partHdr_";
  private static final String PREFIX_HDR = "hdr_";
  private static final String METADATA_PART_HDR_CONTENT_ID = PREFIX_PART_HDR + "Content-Id";
  private static final String KEY_METADATA_SUBJECT = PREFIX_HDR + "Subject";
  private static final String ASSERT_PAYLOAD = "Checking PAYLOAD match";
  private static final String ASSERT_MSG_SUBJECT_HEADER = "Checking Subject Header from MimeMessage";
  private static final String ASSERT_PART_CID_HEADER = "Checking Content-Id Header from selected part";

  private static final PartSelector[] SELECTORS =
  {
      new com.adaptris.util.text.mime.SelectByPosition(0), new com.adaptris.util.text.mime.SelectByContentId("MyContentId"),
      new com.adaptris.util.text.mime.SelectByHeader("Content-Type", "text/xml")
  };


  // overriding to use the list alternative...
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {

    List result = new ArrayList();
    for (int i = 0; i < SELECTORS.length; i++) {
      MimePartSelector mps = createBaseService();
      mps.setSelector(SELECTORS[i]);
      result.add(mps);
    }
    return result;
  }

  @Override
  protected String createBaseFileName(Object o) {
    if (!(o instanceof MimePartSelector)) {
      return super.createBaseFileName(o);
    }
    PartSelector s = ((MimePartSelector) o).getSelector();
    return o.getClass().getName() + "-" + s.getClass().getSimpleName();
  }

  @Test
  public void testInit() throws Exception {
    MimePartSelector mps = createBaseService();
    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(10));
    LifecycleHelper.init(mps);
    try {
      mps = createBaseService();
      LifecycleHelper.init(mps);
      fail("init with no Part Selector");
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testSelectWithNoMatch() throws Exception {
    MimePartSelector mps = createBaseService();
    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(10));
    AdaptrisMessage msg = create();
    String original = msg.getContent();
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, original, msg.getContent());
    assertFalse(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertFalse(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));
  }

  @Test
  public void testSelectByContentId() throws Exception {
    MimePartSelector mps = createBaseService();

    mps.setSelector(new com.adaptris.util.text.mime.SelectByContentId(PART3_CONTENT_ID));
    AdaptrisMessage msg = create();
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, PAYLOAD_3, msg.getContent());
    assertTrue(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertTrue(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));
  }

  @Test
  public void testSelectByHeader() throws Exception {
    MimePartSelector mps = createBaseService();

    mps.setSelector(new com.adaptris.util.text.mime.SelectByHeader(HEADER_CONTENT_ENCODING, ".*base64.*"));
    AdaptrisMessage msg = create();
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, PAYLOAD_1, msg.getContent());
    assertTrue(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertTrue(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));
  }

  @Test
  public void testSelectByPosition() throws Exception {
    MimePartSelector mps = createBaseService();

    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(1));
    AdaptrisMessage msg = create();
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, PAYLOAD_2, msg.getContent());
    assertTrue(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertTrue(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));
  }

  @Test
  public void testSelect_NoHeaderMetadata() throws Exception {
    MimePartSelector mps = createBaseService();
    mps.setPreserveHeadersAsMetadata(false);
    mps.setPreservePartHeadersAsMetadata(true);
    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(1));
    AdaptrisMessage msg = create();
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, PAYLOAD_2, msg.getContent());
    assertFalse(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertTrue(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));
  }

  @Test
  public void testSelect_NoPartHeaderMetadata() throws Exception {
    MimePartSelector mps = createBaseService();
    mps.setPreserveHeadersAsMetadata(true);
    mps.setPreservePartHeadersAsMetadata(false);
    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(1));
    AdaptrisMessage msg = create();
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, PAYLOAD_2, msg.getContent());
    assertTrue(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertFalse(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));

  }

  @Test
  public void testSelect_MarkAsNotMime() throws Exception {
    MimePartSelector mps = createBaseService();
    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(1));
    mps.setMarkAsNonMime(true);
    AdaptrisMessage msg = create();
    assertTrue("Should be marked as a MIME Message", msg.containsKey(CoreConstants.MSG_MIME_ENCODED));
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, PAYLOAD_2, msg.getContent());
    assertTrue(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertTrue(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));
    assertFalse("Should no longer be marked as a MIME Message", msg.containsKey(CoreConstants.MSG_MIME_ENCODED));
  }

  @Test
  public void testSelect_MarkAsNotMime_OriginalWasNotMarked() throws Exception {
    MimePartSelector mps = createBaseService();
    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(1));
    mps.setMarkAsNonMime(true);
    AdaptrisMessage msg = create();
    msg.removeMetadata(new MetadataElement(CoreConstants.MSG_MIME_ENCODED, "true"));
    assertFalse("Should be not marked as a MIME Message", msg.containsKey(CoreConstants.MSG_MIME_ENCODED));
    execute(mps, msg);
    assertEquals(ASSERT_PAYLOAD, PAYLOAD_2, msg.getContent());
    assertTrue(ASSERT_MSG_SUBJECT_HEADER, msg.containsKey(KEY_METADATA_SUBJECT));
    assertTrue(ASSERT_PART_CID_HEADER, msg.containsKey(METADATA_PART_HDR_CONTENT_ID));
    assertFalse("Should no longer be marked as a MIME Message", msg.containsKey(CoreConstants.MSG_MIME_ENCODED));
  }

  @Test
  public void testService_MessageIsNotMime() throws Exception {
    MimePartSelector mps = createBaseService();
    mps.setSelector(new com.adaptris.util.text.mime.SelectByPosition(1));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("The Quick Brown Fox");
    try {
      execute(mps, msg);
      fail("no ServiceException??");
    }
    catch (ServiceException expected) {
      ;
    }
  }

  private static MimePartSelector createBaseService() {
    MimePartSelector mps = new MimePartSelector();
    mps.setHeaderPrefix(PREFIX_HDR);
    mps.setPartHeaderPrefix(PREFIX_PART_HDR);
    mps.setPreserveHeadersAsMetadata(true);
    mps.setPreservePartHeadersAsMetadata(true);
    return mps;
  }
}
