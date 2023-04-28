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

package com.adaptris.core.services.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.internet.MimeBodyPart;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.services.aggregator.MessageAggregatorTest.EvenOddCondition;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.util.text.mime.BodyPartIterator;

public abstract class MimeAggregatorCase extends AggregatorCase {

  protected static final String PAYLOAD = "Pack my box with five dozen liquor jugs.";


  @Test
  public void testSetters() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    assertNull(aggr.getEncoding());
    aggr.setEncoding("base64");
    assertEquals("base64", aggr.getEncoding());
  }

  @Test
  public void testJoinMessage_NoOverwriteMetadata() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(false);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("originalValue", original.getMetadataValue("originalKey"));
  }

  @Test
  public void testJoinMessage_OverwriteMetadata() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(true);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("newValue", original.getMetadataValue("originalKey"));
  }

  @Test
  public void testJoinMessage_ContentType() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setPartContentType("%message{MyContentType}");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("MyContentType", "application/xml"));
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>", null, new HashSet<>(metadata));
    AdaptrisMessage s1 =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>", null, new HashSet<>(metadata));
    AdaptrisMessage s2 =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>", null, new HashSet<>(metadata));
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {s1, s2}));
    BodyPartIterator m = new BodyPartIterator(original.getInputStream());
    for (int i = 0; i < m.size(); i++) {
      MimeBodyPart part = m.getBodyPart(i);
      assertEquals("application/xml", part.getContentType());
    }
  }

  @Test
  public void testJoinMessage_ContentTypeExpression() throws Exception {
    MimeAggregator aggr = createAggregatorForTests().withPartContentType("%message{MyContentType}");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("MyContentType", "application/xml"));
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>",
        null, new HashSet<>(metadata));
    AdaptrisMessage s1 = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("<document>hello</document>", null, new HashSet<>(metadata));
    AdaptrisMessage s2 = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("<document>world</document>", null, new HashSet<>(metadata));
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {s1, s2}));
    BodyPartIterator m = new BodyPartIterator(original.getInputStream());
    for (int i = 0; i < m.size(); i++) {
      MimeBodyPart part = m.getBodyPart(i);
      assertEquals("application/xml", part.getContentType());
    }
  }

  @Test
  public void testJoinMessage_ContentId() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setPartContentId("%message{MyContentId}");
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>", null,
        new HashSet<>(Arrays.asList(new MetadataElement[] {
            new MetadataElement("MyContentId", getName() + "_original")
        })));

    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage s1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>", null,
        new HashSet<>(Arrays.asList(new MetadataElement[] {
            new MetadataElement("MyContentId", getName() + "_split1")
        })));
    AdaptrisMessage s2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>", null,
        new HashSet<>(Arrays.asList(new MetadataElement[] {
            new MetadataElement("MyContentId", getName() + "_split2")
        })));

    List<String> expectedContentIDs =
        new ArrayList<>(Arrays.asList(new String[] {getName() + "_original", getName() + "_split1", getName() + "_split2"
    }));
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {s1, s2}));
    BodyPartIterator m = new BodyPartIterator(original.getInputStream());
    for (int i = 0; i < m.size(); i++) {
      MimeBodyPart part = m.getBodyPart(i);
      assertTrue(expectedContentIDs.contains(part.getContentID()));
    }
  }

  @Test
  public void testJoinMessage_ContentIdExpression() throws Exception {
    MimeAggregator aggr = createAggregatorForTests().withPartContentId("%message{MyContentId}");
    aggr.setPartContentId("%message{MyContentId}");
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>",
        null, new HashSet<>(Arrays.asList(
            new MetadataElement[] {new MetadataElement("MyContentId", getName() + "_original")})));

    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage s1 = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("<document>hello</document>", null, new HashSet<>(Arrays.asList(
            new MetadataElement[] {new MetadataElement("MyContentId", getName() + "_split1")})));
    AdaptrisMessage s2 = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("<document>world</document>", null, new HashSet<>(Arrays.asList(
            new MetadataElement[] {new MetadataElement("MyContentId", getName() + "_split2")})));

    List<String> expectedContentIDs = new ArrayList<>(Arrays.asList(
        new String[] {getName() + "_original", getName() + "_split1", getName() + "_split2"}));
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {s1, s2}));
    BodyPartIterator m = new BodyPartIterator(original.getInputStream());
    for (int i = 0; i < m.size(); i++) {
      MimeBodyPart part = m.getBodyPart(i);
      assertTrue(expectedContentIDs.contains(part.getContentID()));
    }
  }

  @Test
  public void testJoinMessage_Fails() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    AdaptrisMessage original = new DefectiveMessageFactory().newMessage("<envelope/>");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    try {
      aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
      {
          splitMsg1, splitMsg2
      }));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testJoinMessage_PartHeaderFilter() throws Exception {
    MimeAggregator aggr = createAggregatorForTests().withPartHeaderFilter(new NoOpMetadataFilter());
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>",
        null,
        new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-original"))));
    AdaptrisMessage s1 = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("<document>hello</document>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-split1"))));
    AdaptrisMessage s2 = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("<document>world</document>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-split2"))));
    aggr.joinMessage(original, Arrays.asList(s1, s2));
    String payload = original.getContent();
    assertTrue(payload.contains("ZZLC-split1"));
    assertTrue(payload.contains("ZZLC-split2"));
  }

  @Test
  public void testJoinMessage_MimeHeaderFilter() throws Exception {
    MimeAggregator aggr = createAggregatorForTests().withMimeHeaderFilter(new NoOpMetadataFilter());
    AdaptrisMessage original =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-original"))));
    AdaptrisMessage s1 =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-split1"))));
    AdaptrisMessage s2 =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-split2"))));
    aggr.joinMessage(original, Arrays.asList(s1, s2));
    String payload = original.getContent();
    assertTrue(payload.contains("ZZLC-original"));
    assertFalse(payload.contains("ZZLC-split1"));
    assertFalse(payload.contains("ZZLC-split2"));
  }

  @Test
  public void testJoinMessage_WithSubType() throws Exception {
    MimeAggregator aggr = createAggregatorForTests().withMimeContentSubType("form-data");
    AdaptrisMessage original =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-original"))));
    AdaptrisMessage s1 =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-split1"))));
    AdaptrisMessage s2 =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>", null,
            new HashSet<>(Arrays.asList(new MetadataElement("X-Interlok-Test", "ZZLC-split2"))));
    aggr.joinMessage(original, Arrays.asList(s1, s2));
    String payload = original.getContent();
    System.err.println(payload);
    assertTrue(payload.contains("multipart/form-data"));
  }

  @Test
  public void testAggregate_WithFilter() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(false);
    aggr.setFilterCondition(new EvenOddCondition());
    AdaptrisMessageFactory fac = AdaptrisMessageFactory.getDefaultInstance();
    AdaptrisMessage original = fac.newMessage("<envelope/>");
    AdaptrisMessage splitMsg1 = fac.newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = fac.newMessage("<document>world</document>");
    aggr.aggregate(original, Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2}));
    String payload = original.getContent();

    assertFalse(payload.contains("hello"));
    assertTrue(payload.contains("world"));
  }



  @Override
  protected MimeAggregator createAggregatorForTests() {
    return new MimeAggregator();
  }

  protected AddMetadataService createAddMetadataService(String key) {
    AddMetadataService service = new AddMetadataService();
    service.addMetadataElement(key, "$UNIQUE_ID$");
    return service;
  }
}
