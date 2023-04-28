package com.adaptris.core.services.metadata;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

/**
 * @author mwarman
 */
public class DateFormatBuilderWithOptionalFractionAndOffsetTest {
  @Test
  public void testFormatsWithOutError() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    DateFormatBuilder.DateFormatter formatter = new DateFormatBuilderWithOptionalFractionAndOffset().withFormat("yyyy-MM-dd'T'HH:mm:ss").build(message);
    try {
      formatter.toDate("2016-08-24T22:35:26.324114Z");
      formatter.toDate("2016-08-24T22:35:26.324114");
      formatter.toDate("2016-08-24T22:35:26Z");
      formatter.toDate("2016-08-24T22:35:26");
    } catch (ParseException e){
      fail();
    }
    assertTrue(formatter.toString(new Date(1536753569000L)).startsWith("2018"));
  }


  @Test
  public void testFormat_WithLocale_Timezone() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    DateFormatBuilder.DateFormatter formatter =
        new DateFormatBuilderWithOptionalFractionAndOffset().withLanguageTag("en-GB").withTimezone("GMT")
            .withFormat("yyyy-MM-dd'T'HH:mm:ss").build(message);
    try {
      formatter.toDate("2016-08-24T22:35:26.324114Z");
      formatter.toDate("2016-08-24T22:35:26.324114");
      formatter.toDate("2016-08-24T22:35:26Z");
      formatter.toDate("2016-08-24T22:35:26");
    } catch (ParseException e) {
      fail();
    }
    assertTrue(formatter.toString(new Date(1536753569000L)).startsWith("2018"));
  }

  @Test
  public void testFormatsWithError() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    DateFormatBuilder.DateFormatter formatter =
        new DateFormatBuilderWithOptionalFractionAndOffset().withFormat("yyyy-MM-dd'T'HH:mm:ss").build(message);
    try {
      formatter.toDate("2016-08-24X22:35:26.324114Z");
      fail();
    } catch (ParseException e) {
    }
  }

}