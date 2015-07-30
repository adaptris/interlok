package com.adaptris.core.mail.attachment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeUtility;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Handle attachments for {@link MultiAttachmentSmtpProducer}.
 * 
 * @config mail-xml-attachment-handler
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mail-xml-attachment-handler")
public class XmlAttachmentHandler implements AttachmentHandler {
  @NotNull
  @NotBlank
  private String xpath;
  private String filenameXpath;
  private String encodingXpath;
  private transient IdGenerator idGenerator = null;
  private KeyValuePairSet namespaceContext;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());

  public XmlAttachmentHandler() {
    idGenerator = new GuidGenerator();
  }

  public XmlAttachmentHandler(String xpath, String fnameXpath) {
    this();
    setXpath(xpath);
    setFilenameXpath(fnameXpath);
  }

  public XmlAttachmentHandler(String xpath, String filenameXpath, String encoding) {
    this(xpath, filenameXpath);
    setEncodingXpath(encoding);
  }

  /**
   * @return the xpath
   */
  public String getXpath() {
    return xpath;
  }

  /**
   * The XPath that will produce one or more attachments.
   *
   * @param s the xpath to set
   */
  public void setXpath(String s) {
    xpath = s;
  }

  /**
   * @return the filenameXpath
   */
  public String getFilenameXpath() {
    return filenameXpath;
  }

  /**
   * The Xpath that determines the file name associated with this attachment.
   *
   * @param filenameXpath the filenameXpath to set
   */
  public void setFilenameXpath(String filenameXpath) {
    this.filenameXpath = filenameXpath;
  }

  @Override
  public List<MailAttachment> resolve(Document doc) throws Exception {
    XPath x = new XPath(SimpleNamespaceContext.create(getNamespaceContext()));
    List<MailAttachment> result = new ArrayList<MailAttachment>();
    logR.trace("Resolving " + getXpath());
    NodeList nl = x.selectNodeList(doc, getXpath());
    if (nl == null) {
      return result;
    }
    else {
      logR.trace("Found " + nl.getLength() + " attachments");
    }
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      n.normalize();
      String filename = null;
      if (getFilenameXpath() != null) {
        filename = x.selectSingleTextItem(n, getFilenameXpath());
        logR.trace("Found filename [" + filename + "] from XPath [" + getFilenameXpath() + "]");
      }
      if (filename == null) {
        filename = idGenerator.create(n);
        logR.warn("Could not determine filename for MimeBodyPart, " + "assigning unique filename of " + filename);
      }
      result.add(new MailAttachment(getData(n), filename));
    }
    return result;
  }

  protected byte[] getData(Node n) throws Exception {
    XPath x = new XPath(SimpleNamespaceContext.create(getNamespaceContext()));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    n.normalize();
    String s = n.getTextContent();
    ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
    InputStream encodedIn = in;
    if (getEncodingXpath() != null) {
      String encoding = x.selectSingleTextItem(n, getEncodingXpath());
      logR.trace("Found encoding type [" + encoding + "] from XPath [" + getEncodingXpath() + "]");
      encodedIn = MimeUtility.decode(in, encoding);
    }
    StreamUtil.copyStream(encodedIn, out);
    out.flush();
    return out.toByteArray();
  }

  /**
   * @return the encodingXpath
   */
  public String getEncodingXpath() {
    return encodingXpath;
  }

  /**
   * If specified the value returned by the xpath will be used to decode the
   * contents of the attachment xpath.
   *
   * @param s the encodingXpath to set
   */
  public void setEncodingXpath(String s) {
    encodingXpath = s;
  }

  /**
   * @return the namespaceContext
   */
  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param kvps the namespace context
   * @see SimpleNamespaceContext#create(KeyValuePairSet)
   */
  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }
}
