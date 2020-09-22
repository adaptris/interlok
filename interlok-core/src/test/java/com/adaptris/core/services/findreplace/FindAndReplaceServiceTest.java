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

package com.adaptris.core.services.findreplace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class FindAndReplaceServiceTest extends GeneralServiceExample {

  private static final String TO_REPLACE = "test";
  private static final String REPLACE_WITH = "joke";
  private static final String STANDARD_PAYLOAD = "This is a test! A test!";
  private static final String PAYLOAD_REPLACED_FIRST_ONLY = "This is a joke! A test!";
  private static final String PAYLOAD_REPLACED_ALL = "This is a joke! A joke!";

  private static final String PAYLOAD_REPLACED_FIRST_ONLY_HEX = "This is a test. A test!";
  private static final String PAYLOAD_REPLACED_ALL_HEX = "This is a test. A test.";

  // Example payload for matchgroups {"40.8682,-86.3812":{"current_conditions":{"air_temp":{"unit":"F","value":32.0}}}}
  private static final String REGEXP_PAYLOAD = "{\"40.8682,-86.3812\":{\"current_conditions\":{\"air_temp\":{\"unit\":\"F\",\"value\":32.0}}}}";
  private static final String REGEXP_FIND_WITH_MATCHGROUP = "\\{\"([\\-0-9]+\\.[0-9]+),([\\-0-9]+\\.[0-9]+)\":";
  private static final String REGEXP_REPLACE_USE_MATCHGROUP = "{ latitude: \"$1\", longitude: \"$2\", forecast:";
  // Should be { latitude: "40.8682", longitude: "-86.3812", forecast:{"current_conditions":{"air_temp":{"unit":"F","value":32.0}}}}
  // after doing a regexp replace with matchgroups.
  private static final String REGEXP_PAYLOAD_EXPECTED = "{ latitude: \"40.8682\", longitude: \"-86.3812\", forecast:{\"current_conditions\":{\"air_temp\":{\"unit\":\"F\",\"value\":32.0}}}}";

  private enum ReplacementSourceImpl {
    Hexadecimal {
      @Override
      public ReplacementSource getFindImpl() {
        HexSequenceConfiguredReplacementSource source = new HexSequenceConfiguredReplacementSource("21", "UTF-8");
        return source;
      }

      @Override
      public ReplacementSource getReplaceImpl() {
        HexSequenceConfiguredReplacementSource source = new HexSequenceConfiguredReplacementSource("2E", "UTF-8");
        return source;
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n" + "The hex-sequence-configured-replacement-source is based on an byte sequence represented "
            + "\nas a sequence of hexadecimal numbers (2 characters per byte)."
            + "\nIn the example below you are replacing the first occurence of hex value 21 (!) with hex value 2E (.)."
            + "\nThe charset configuration may be '' which will then use the platform default to convert the"
            + "\nhexadecimal byte values to Strings; explicitly configuring it is recommended." + "\n-->\n";
      };
    },

    Metadata {
      @Override
      public ReplacementSource getFindImpl() {
        MetadataReplacementSource source = new MetadataReplacementSource(TO_REPLACE);
        return source;
      }

      @Override
      public ReplacementSource getReplaceImpl() {
        MetadataReplacementSource source = new MetadataReplacementSource(REPLACE_WITH);
        return source;
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n" + "The metadata-replacement-source is based around metadata, in the example below"
            + "\nyou are replacing the first occurence of value associated with the metadata-key '" + TO_REPLACE
            + "'\nvalue associated with the key '" + REPLACE_WITH + "'\n-->\n";
      };
    },

    Configured {
      @Override
      public ReplacementSource getFindImpl() {
        ConfiguredReplacementSource source = new ConfiguredReplacementSource(TO_REPLACE);
        return source;
      }

      @Override
      public ReplacementSource getReplaceImpl() {
        ConfiguredReplacementSource source = new ConfiguredReplacementSource(REPLACE_WITH);
        return source;
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n" + "The configured-replacement-source is the simplest type of ReplacementSource implementation, "
            + "\nyou are simply replacing the first occurence of value '" + TO_REPLACE + "' with " + "\nthe value '" + REPLACE_WITH
            + "'\n-->\n";
      }

    };

    public abstract ReplacementSource getFindImpl();

    public abstract ReplacementSource getReplaceImpl();

    public abstract String getXmlHeader();

    public FindAndReplaceUnit getFindAndReplaceUnit() {
      FindAndReplaceUnit unit = new FindAndReplaceUnit();
      unit.setFind(getFindImpl());
      unit.setReplace(getReplaceImpl());
      return unit;
    };

    public ArrayList<FindAndReplaceUnit> getFindAndReplaceUnits() {
      ArrayList<FindAndReplaceUnit> result = new ArrayList<FindAndReplaceUnit>();
      result.add(getFindAndReplaceUnit());
      return result;
    }
  }


  @Test
  public void testSetFindReplaceUnits() throws Exception {
    FindAndReplaceService service = new FindAndReplaceService();
    assertEquals(0, service.getFindAndReplaceUnits().size());

    List<FindAndReplaceUnit> frList = ReplacementSourceImpl.Configured.getFindAndReplaceUnits();
    service.setFindAndReplaceUnits(frList);

    assertEquals(1, service.getFindAndReplaceUnits().size());
    assertTrue(frList.get(0).equals(service.getFindAndReplaceUnits().get(0)));
  }

  @Test
  public void testReplaceFirstConfigured() throws Exception {
    FindAndReplaceService service = createServiceForTests(ReplacementSourceImpl.Configured, true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    execute(service, msg);
    assertTrue(msg.getContent().equals(PAYLOAD_REPLACED_FIRST_ONLY));
  }

  @Test
  public void testReplaceFirst_RegExp_MatchGroups() throws Exception {
    FindAndReplaceService service = new FindAndReplaceService();
    service.setReplaceFirstOnly(true);
    service.getFindAndReplaceUnits().add(
        new FindAndReplaceUnit(new ConfiguredReplacementSource(REGEXP_FIND_WITH_MATCHGROUP), new ConfiguredReplacementSource(
            REGEXP_REPLACE_USE_MATCHGROUP)));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(REGEXP_PAYLOAD);
    execute(service, msg);
    assertEquals(REGEXP_PAYLOAD_EXPECTED, msg.getContent());
  }

  @Test
  public void testReplaceAllConfigured() throws Exception {
    FindAndReplaceService service = createServiceForTests(ReplacementSourceImpl.Configured, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    execute(service, msg);
    assertTrue(msg.getContent().equals(PAYLOAD_REPLACED_ALL));
  }

  @Test
  public void testReplaceAll_RegExp_MatchGroups() throws Exception {
    FindAndReplaceService service = new FindAndReplaceService();
    service.setReplaceFirstOnly(false);
    service.getFindAndReplaceUnits().add(
        new FindAndReplaceUnit(new ConfiguredReplacementSource(REGEXP_FIND_WITH_MATCHGROUP), new ConfiguredReplacementSource(
            REGEXP_REPLACE_USE_MATCHGROUP)));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(REGEXP_PAYLOAD);
    execute(service, msg);
    assertEquals(REGEXP_PAYLOAD_EXPECTED, msg.getContent());
  }

  @Test
  public void testReplaceFirstMetadata() throws Exception {
    FindAndReplaceService service = createServiceForTests(ReplacementSourceImpl.Metadata, true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(TO_REPLACE, TO_REPLACE);
    msg.addMetadata(REPLACE_WITH, REPLACE_WITH);
    execute(service, msg);
    assertTrue(msg.getContent().equals(PAYLOAD_REPLACED_FIRST_ONLY));
  }

  @Test
  public void testReplaceAllMetadata() throws Exception {
    FindAndReplaceService service = createServiceForTests(ReplacementSourceImpl.Metadata, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(TO_REPLACE, TO_REPLACE);
    msg.addMetadata(REPLACE_WITH, REPLACE_WITH);
    execute(service, msg);
    assertTrue(msg.getContent().equals(PAYLOAD_REPLACED_ALL));
  }

  @Test
  public void testReplaceFirstHexadecimal() throws Exception {
    FindAndReplaceService service = createServiceForTests(ReplacementSourceImpl.Hexadecimal, true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    execute(service, msg);
    assertTrue(msg.getContent().equals(PAYLOAD_REPLACED_FIRST_ONLY_HEX));
  }

  @Test
  public void testReplaceAllHexadecimal() throws Exception {
    FindAndReplaceService service = createServiceForTests(ReplacementSourceImpl.Hexadecimal, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    execute(service, msg);
    assertTrue(msg.getContent().equals(PAYLOAD_REPLACED_ALL_HEX));
  }

  private FindAndReplaceService createServiceForTests(ReplacementSourceImpl impl, boolean replaceFirst) {
    FindAndReplaceService service = new FindAndReplaceService();
    service.setFindAndReplaceUnits(impl.getFindAndReplaceUnits());
    service.setReplaceFirstOnly(replaceFirst);
    return service;
  }

  @Override
  protected FindAndReplaceService retrieveObjectForSampleConfig() {
    FindAndReplaceService service = new FindAndReplaceService();
    ArrayList<FindAndReplaceUnit> units = new ArrayList<FindAndReplaceUnit>();
    for (ReplacementSourceImpl impl : ReplacementSourceImpl.values()) {
      units.add(impl.getFindAndReplaceUnit());
    }
    service.setFindAndReplaceUnits(units);
    return service;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    String hdr = super.getExampleCommentHeader(object)
        + "<!--\n The find and replace service replaces parts of the payload with your configured replacement source."
        + "\nNote that you can mix/match the various ReplacementSource implementations."
        + "\nFor instance you might want to search for a Hex sequence and replace it with a value "
        + "derived from metadata, or vice versa." + "\n-->\n";
    for (ReplacementSourceImpl impl : ReplacementSourceImpl.values()) {
      hdr += impl.getXmlHeader();
    }
    return hdr;
  }

  private ReplacementSourceImpl getReplacementImpl(FindAndReplaceService consumer) {
    ReplacementSourceImpl result = null;
    for (ReplacementSourceImpl filter : ReplacementSourceImpl.values()) {
      if (filter.getFindImpl().getClass().equals(consumer.getFindAndReplaceUnits().get(0).getClass())) {
        result = filter;
        break;
      }
    }
    return result;
  }

}
