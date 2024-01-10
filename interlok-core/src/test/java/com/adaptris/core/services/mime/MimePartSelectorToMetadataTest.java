/*
 * Copyright 2018 Adaptris Ltd.
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

import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.PAYLOAD_1;
import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.PAYLOAD_2;
import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.PAYLOAD_3;
import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.create;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.mime.PartSelectorToMetadata.MetadataTarget;
import com.adaptris.util.text.mime.SelectByContentId;
import com.adaptris.util.text.mime.SelectByPosition;

public class MimePartSelectorToMetadataTest extends MimeServiceExample {

  @Override
  protected MimePartSelectorToMetadata retrieveObjectForSampleConfig() {
    return new MimePartSelectorToMetadata();
  }

  @Test
  public void testGetPartSelectorToMetadata() throws Exception {
    MimePartSelectorToMetadata service = new MimePartSelectorToMetadata();
    List<PartSelectorToMetadata> selectors = new ArrayList<>();

    PartSelectorToMetadata partSelectorToMetadataOne = new PartSelectorToMetadata();
    partSelectorToMetadataOne.setMetadataKey("partContentOne");
    partSelectorToMetadataOne.setSelector(new SelectByPosition(0));
    selectors.add(partSelectorToMetadataOne);
    PartSelectorToMetadata partSelectorToMetadataTwo = new PartSelectorToMetadata();
    partSelectorToMetadataTwo.setMetadataKey("partContentTwo");
    partSelectorToMetadataTwo.setSelector(new SelectByContentId("part2"));
    selectors.add(partSelectorToMetadataTwo);
    PartSelectorToMetadata partSelectorToMetadataThree = new PartSelectorToMetadata();
    partSelectorToMetadataThree.setMetadataKey("partContentThree");
    partSelectorToMetadataThree.setSelector(new SelectByContentId("part3"));
    partSelectorToMetadataThree.setMetadataTarget(MetadataTarget.Object);
    selectors.add(partSelectorToMetadataThree);
    PartSelectorToMetadata partSelectorToMetadataDoesntExist = new PartSelectorToMetadata();
    partSelectorToMetadataDoesntExist.setMetadataKey("partContentFour");
    partSelectorToMetadataDoesntExist.setSelector(new SelectByContentId("partDoesntExist"));
    selectors.add(partSelectorToMetadataDoesntExist);

    service.setSelectors(selectors);

    AdaptrisMessage msg = create();
    execute(service, msg);

    assertEquals(PAYLOAD_1, msg.getMetadataValue("partContentOne"));
    assertEquals(PAYLOAD_2, msg.getMetadataValue("partContentTwo"));
    assertArrayEquals(PAYLOAD_3.getBytes(), (byte[]) msg.getObjectHeaders().get("partContentThree"));
    assertNull(msg.getMetadataValue("partContentFour"));
  }

  @Test
  public void testGetPartSelectorToMetadataInvalidMime() throws Exception {
    MimePartSelectorToMetadata service = new MimePartSelectorToMetadata();
    List<PartSelectorToMetadata> selectors = new ArrayList<>();

    PartSelectorToMetadata partSelectorToMetadataOne = new PartSelectorToMetadata();
    partSelectorToMetadataOne.setMetadataKey("partContentOne");
    partSelectorToMetadataOne.setSelector(new SelectByPosition(0));
    selectors.add(partSelectorToMetadataOne);
    PartSelectorToMetadata partSelectorToMetadataTwo = new PartSelectorToMetadata();
    partSelectorToMetadataTwo.setMetadataKey("partContentTwo");
    partSelectorToMetadataTwo.setSelector(new SelectByContentId("part2"));
    selectors.add(partSelectorToMetadataTwo);
    PartSelectorToMetadata partSelectorToMetadataThree = new PartSelectorToMetadata();
    partSelectorToMetadataThree.setMetadataKey("partContentThree");
    partSelectorToMetadataThree.setSelector(new SelectByContentId("part3"));
    partSelectorToMetadataThree.setMetadataTarget(MetadataTarget.Object);
    selectors.add(partSelectorToMetadataThree);
    PartSelectorToMetadata partSelectorToMetadataDoesntExist = new PartSelectorToMetadata();
    partSelectorToMetadataDoesntExist.setMetadataKey("partContentFour");
    partSelectorToMetadataDoesntExist.setSelector(new SelectByContentId("partDoesntExist"));
    selectors.add(partSelectorToMetadataDoesntExist);

    service.setSelectors(selectors);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(CoreConstants.MSG_MIME_ENCODED, "true");

    assertThrows(ServiceException.class, () -> execute(service, msg));
  }

  @Test
  public void testPartSelectorToMetadataSetNullMetadataKey() throws Exception {
    PartSelectorToMetadata partSelectorToMetadataThree = new PartSelectorToMetadata();
    assertThrows(NullPointerException.class, () -> partSelectorToMetadataThree.setMetadataKey(null));
  }

  @Test
  public void testPartSelectorToMetadataSetNullSelector() throws Exception {
    PartSelectorToMetadata partSelectorToMetadataThree = new PartSelectorToMetadata();
    assertThrows(NullPointerException.class, () -> partSelectorToMetadataThree.setSelector(null));
  }

  @Test
  public void testPartSelectorToMetadataSetNullMetadataTarget() throws Exception {
    PartSelectorToMetadata partSelectorToMetadataThree = new PartSelectorToMetadata();
    assertThrows(NullPointerException.class, () -> partSelectorToMetadataThree.setMetadataTarget(null));
  }

}
