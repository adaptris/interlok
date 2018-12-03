package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;

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
}