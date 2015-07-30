package com.adaptris.util.text.mime;

import java.util.List;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link PartSelector} implementation that just selects an arbitrary part.
 * 
 * <p>
 * No guarantees are made about the behaviour of this implementation; it is simply included for completenes
 * </p>
 * 
 * @config mime-null-selector
 * 
 * @author lchan
 * 
 */
@XStreamAlias("mime-null-selector")
public class NullPartSelector implements PartSelector {

	public NullPartSelector() {
	}

  /**
   * Returns the first part of the {@link MultiPartInput}
   * 
   */
	@Override
	public MimeBodyPart select(MultiPartInput m) throws MessagingException {
    return m.getBodyPart(0);
	}

  /**
   * Returns all the parts in the {@link MimeMultipart}.
   */
	@Override
	public List<MimeBodyPart> select(MimeMultipart in) throws MessagingException {
		Vector<MimeBodyPart> list = new Vector<MimeBodyPart>();
		for (int i=0;i < in.getCount(); i++){
			list.add((MimeBodyPart)in.getBodyPart(i));
		}
		return list;
	}
}
