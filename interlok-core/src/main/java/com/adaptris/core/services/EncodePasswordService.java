package com.adaptris.core.services;

import com.adaptris.annotation.*;
import com.adaptris.core.*;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.fs.FsException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import static com.adaptris.fs.FsWorker.checkReadable;
import static com.adaptris.fs.FsWorker.isFile;

/**
 * Encodes password to use Portable_Password_2 encoding based on configurations into the message payload.
 *
 * @config encode-password-service
 */
@AdapterComponent
@ComponentProfile(summary = "Encodes passwords for file paths passed into the message payload",
    tag = "service,file")
@XStreamAlias("encode-password-service")
public class EncodePasswordService extends ServiceImp {

  /**
   * The parameter for the path to the file to read.
   */
  @Getter
  @Setter
  @InputFieldHint(expression = false)
  private String filePath;

  @Getter
  @Setter
  @InputFieldHint(expression = false)
  private String[] keys = {};

  private static final String PARAMS_FILE_PATH = "filePath";
  private static final String PARAMS_KEYS = "keys";
  private static final String PREFIX_PORTABLE_PASSWORD = "PW:";
  private static final String PREFIX_PORTBALE_PASSWORD_2 = "AES_GCM:";
  private static final String EXTN_XML = ".xml";
  private static final String EXTN_PROPERTIES = ".properties";
  private static final String METADATA_KEY_PASSWORD_TOKENS = "passwordtokens";

  private transient DocumentBuilderFactory dbFactory;


  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getFilePath(), PARAMS_FILE_PATH);
      Args.notBlank(Arrays.toString(getKeys()), PARAMS_KEYS);

      dbFactory = DocumentBuilderFactory.newInstance();
      dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }


  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {
    log.trace("Encoding file service");
    //Collect values for metadata key - password tokens - if passed any
    setKeys(message.getMetadata().stream().filter(e -> e.getKey().equals(METADATA_KEY_PASSWORD_TOKENS))
            .collect(Collectors.toList()).get(0).getValue().split(","));

    try {
      final File file = convertToFile(message.resolve(getFilePath()));
      log.info("File in process : {}", file.getName());
      if(file.getName().endsWith(EXTN_PROPERTIES)) {
        encodeValuesInPropertiesFile(message);
      } else if(file.getName().endsWith(EXTN_XML)) {
        encodeValuesInXmlFile(file);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    /* empty method */
  }

  @Override
  protected void closeService() {
    /* empty method */
  }

  /**
   * Finds and encodes values in the properties contained in Adaptris Message
   *
   * @param message
   * @throws IOException
   */
  private void encodeValuesInPropertiesFile(AdaptrisMessage message) throws IOException {
    StringBuilder sb = new StringBuilder();
    List<String> lines = Files.readAllLines(Paths.get(message.resolve(getFilePath())));
    lines.forEach(line -> {
      if (line.contains("=")) {
        String key = line.substring(0, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1).trim();
        if (isPasswordKey(key.toLowerCase())) {
          try {
            if (value.startsWith(PREFIX_PORTABLE_PASSWORD)) {
              value = Password.encode(Password.decode(value), Password.PORTABLE_PASSWORD_2);
            } else if (!value.startsWith(PREFIX_PORTBALE_PASSWORD_2)) { //Plain text
              value = Password.encode(value, Password.PORTABLE_PASSWORD_2);
            }
          } catch (PasswordException e) {
            log.debug("Password could not be decoded", e);
          }
        }
        sb.append(key).append("=").append(value);
      } else {
        sb.append(line);
      }
      sb.append(System.lineSeparator());
    });
    Files.write(Paths.get(message.resolve(getFilePath())), sb.toString().getBytes());
  }

  /**
   *  Finds and encodes value contained in an XML file
   *
   * @param file
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws TransformerException
   */
  private void encodeValuesInXmlFile(File file) throws ParserConfigurationException, SAXException, IOException, TransformerException {
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(file);
    doc.getDocumentElement().normalize();

    // Process the root element
    replaceNodeValues(doc.getDocumentElement());

    // Write the updated XML to a new file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(file);
    transformer.transform(source, result);
  }


  /**
   * Recursive method to replace values in nodes that match the pattern
   *
   * @param node
   */
  private void replaceNodeValues(Node node) {

    log.trace("Inside replaceNodeValues method - node: {}", node.getNodeName());

    //Check and replace children nodes
    NodeList childNodes = node.getChildNodes();
    for (int idx = 0; idx < childNodes.getLength(); idx++) {
      replaceNodeValues(childNodes.item(idx));
    }

    // Process current node
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      String nodeName = node.getNodeName();
      String nodeValue = node.getTextContent();

      // Check if the node name matches the set of password passphrases
      if (isPasswordKey(nodeName)) {
        try {
          if (nodeValue.startsWith(PREFIX_PORTABLE_PASSWORD)) {
            nodeValue = Password.encode(Password.decode(nodeValue), Password.PORTABLE_PASSWORD_2);
          } else if (!nodeValue.startsWith(PREFIX_PORTBALE_PASSWORD_2) && !(nodeValue.startsWith("${") && nodeValue.endsWith("}"))) { //Plain text
            nodeValue = Password.encode(nodeValue, Password.PORTABLE_PASSWORD_2);
          }
        } catch (PasswordException e) {
          log.debug("Password could not be decoded", e);
        }
        node.setTextContent(nodeValue);
      }
    }
  }

  /**
   * Converts file path to File object
   *
   * @param filepath
   * @return
   * @throws FsException
   */
  @SuppressWarnings({"lgtm [java/path-injection]"})
  private File convertToFile(String filepath) throws FsException {
    try {
      return isFile(checkReadable(FsHelper.toFile(filepath)));
    } catch (Exception e) {
      return isFile(checkReadable(new File(filepath)));
    }
  }

  /**
   * Checks if the input is a password passphrase
   *
   * @param name
   * @return
   */
  private boolean isPasswordKey(String name) {
    boolean result = false;

    for(String passphraseKey : getKeys()) {
      //Check if the passed name matches with set of passphrase keys
      if(StringUtils.isNotEmpty(passphraseKey) &&
              (name.toLowerCase().trim().startsWith(passphraseKey.toLowerCase().trim()) || name.toLowerCase().endsWith(passphraseKey.toLowerCase().trim()))) {
        result = true;
      }
    }
    return result;
  }
}
