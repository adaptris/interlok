package com.adaptris.core.services.exception;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;

import java.io.OutputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.w3c.dom.Document;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.adaptris.util.text.xml.DocumentMerge;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that takes an exception in object metadata and creates an XML document that can be then inserted into the AdaptrisMessage
 * payload.
 * 
 * <p>
 * In some scenarios (e.g. handling request reply scenarios), rather than directly handling the message exception in the adapter, it
 * may be required to report back to the back-end application that an exception has occured along with the problem document.
 * Assuming that the original document is in XML format then this service allows you to do generate a simple XML report from the
 * exception itself, and merge that into the document as you see fit.
 * </p>
 * 
 * @config exception-report-service
 * @license STANDARD
 * @see ExceptionReportGenerator
 * @see DocumentMerge
 * @author lchan
 * @see CoreConstants#OBJ_METADATA_EXCEPTION
 */
@XStreamAlias("exception-report-service")
public class ExceptionReportService extends ServiceImp {

  @NotNull
  @Valid
  private DocumentMerge documentMerge;
  @NotNull
  @Valid
  private ExceptionReportGenerator exceptionGenerator;
  @NotBlank
  @AutoPopulated
  private String xmlEncoding;

  public ExceptionReportService() {
    setXmlEncoding("UTF-8");
  }

  public ExceptionReportService(ExceptionReportGenerator generator, DocumentMerge merge) {
    this();
    setExceptionGenerator(generator);
    setDocumentMerge(merge);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    OutputStream out = null;
    try {
      if (msg.getObjectMetadata().containsKey(OBJ_METADATA_EXCEPTION)) {
        Exception e = (Exception) msg.getObjectMetadata().get(OBJ_METADATA_EXCEPTION);
        Document newDoc = getExceptionGenerator().create(e);
        Document result = getDocumentMerge().merge(XmlHelper.createDocument(msg, true), newDoc);
        out = msg.getOutputStream();
        new XmlUtils().writeDocument(result, out, getXmlEncoding());
        msg.setCharEncoding(getXmlEncoding());
      }
      else {
        log.debug("No Exception in object metadata, nothing to do.");
      }
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  public void init() throws CoreException {
    if (documentMerge == null) {
      throw new CoreException("No documentMerge");
    }
    if (exceptionGenerator == null) {
      throw new CoreException("No Configured exception xml generator");
    }
  }

  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  public void close() {
  }

  @Override
  public boolean isEnabled(License l) {
    return l.isEnabled(LicenseType.Standard);
  }

  public DocumentMerge getDocumentMerge() {
    return documentMerge;
  }

  /**
   * Specify how to merge the exception into the AdaptrisMessage.
   *
   * @param m the merge implementation
   */
  public void setDocumentMerge(DocumentMerge m) {
    documentMerge = m;
  }

  public ExceptionReportGenerator getExceptionGenerator() {
    return exceptionGenerator;
  }

  /**
   * Specify how to create the XML document from the exception.
   *
   * @param generator the generator.
   */
  public void setExceptionGenerator(ExceptionReportGenerator generator) {
    exceptionGenerator = generator;
  }

  public String getXmlEncoding() {
    return xmlEncoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   *
   * @param encoding the encoding, default is UTF-8
   */
  public void setXmlEncoding(String encoding) {
    xmlEncoding = encoding;
  }
}
