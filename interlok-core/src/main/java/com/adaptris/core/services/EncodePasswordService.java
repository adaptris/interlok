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
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.validation.constraints.NotBlank;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.*;

import static com.adaptris.fs.FsWorker.checkReadable;
import static com.adaptris.fs.FsWorker.isFile;

/**
 * Encodes password based on configurations into the message payload.
 *
 * @config encode-password-service
 */
@AdapterComponent
@ComponentProfile(summary = "Encodes a file from a specific path into the message payload",
    tag = "service,file")
@XStreamAlias("encode-password-service")
public class EncodePasswordService extends ServiceImp {

  /**
   * The parameter for the path to the file to read.
   */
  @NotBlank
  @InputFieldHint(expression = true)
  private String filePath;


  private String[] keys = {};
  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {

    log.info("Encoding file service - :");
    keys = message.getMetadata().stream().filter(e -> e.getKey().equals("passwordtokens")).collect(Collectors.toList()).get(0).getValue().split(",");

    try {
      final File file = convertToFile(message.resolve(getFilePath()));
      StringBuilder sb = new StringBuilder();

      if(file.getName().endsWith(".properties")) {
        List<String> lines = Files.readAllLines(Paths.get(message.resolve(getFilePath())));
        lines.forEach(line -> {
          if (line.indexOf("=") != -1) {
            String key = line.substring(0, line.indexOf("=")).trim();
            String value = line.substring(line.indexOf("=") + 1).trim();
            if (isPasswordKey(key.toLowerCase())) {
              try {
                if (value.startsWith("PW:")) {
                  value = Password.encode(Password.decode(value), Password.PORTABLE_PASSWORD_2);
                } else if (!value.startsWith("AES_GCM:")) { //Plain text
                  value = Password.encode(value, Password.PORTABLE_PASSWORD_2);
                }
              } catch (PasswordException e) {
                log.debug("Password could not be decoded", e);
              }
            }
            sb.append(key + "=" + value);
          } else {
            sb.append(line);
          }
          sb.append(System.lineSeparator());
        });
        Files.write(Paths.get(message.resolve(getFilePath())), sb.toString().getBytes());
      } else if(file.getName().endsWith(".xml")) {
        log.info("File XML - : {}", file.getName());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        // Pattern to match (example: all nodes that start with "data")
        String nodeNamePattern = "password";

        // Process the root element
        replaceNodeValues(doc.getDocumentElement(), nodeNamePattern);

        // Write the updated XML to a new file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
      }

    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private boolean isPasswordKey(String key) {
    boolean result = false;

    for(String k : keys) {
      if(StringUtils.isNotEmpty(k) && (key.toLowerCase().trim().startsWith(k.toLowerCase().trim()) || key.toLowerCase().endsWith(k.toLowerCase().trim()))) {
        result = true;
      }
    }
    return result;
  }

  // Recursive method to replace values in nodes that match the pattern
  private void replaceNodeValues(Node node, String pattern) {
    log.info("Inside replaceNodeValues method - node: {}", node.getNodeName());
    // Process current node
    if (node.getNodeType() == Node.ELEMENT_NODE) {

      String nodeName = node.getNodeName();
      String nodeValue = node.getTextContent();

      log.info("Replace nod ename: {}, value: {}", nodeName, nodeValue);

      // Check if the node name matches the pattern
      if (nodeName.contains(pattern)) {
        // For elements that match, set text content to the new value
        if (isPasswordKey(nodeName)) {
          try {
            if (nodeValue.startsWith("PW:")) {
              nodeValue = Password.encode(Password.decode(nodeValue), Password.PORTABLE_PASSWORD_2);
            } else if (!nodeValue.startsWith("AES_GCM:")) { //Plain text
              nodeValue = Password.encode(nodeValue, Password.PORTABLE_PASSWORD_2);
            }
          } catch (PasswordException e) {
            log.debug("Password could not be decoded", e);
          }
        }
        node.setTextContent(nodeValue);
      }
    }

    // Process child nodes recursively
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      replaceNodeValues(childNodes.item(i), pattern);
    }
  }

  @SuppressWarnings({"lgtm [java/path-injection]"})
  private static File convertToFile(String filepath) throws FsException {
    try {
      return isFile(checkReadable(FsHelper.toFile(filepath)));
    } catch (Exception e) {
      return isFile(checkReadable(new File(filepath)));
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

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getFilePath(), "filePath");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * Get the file path parameter.
   *
   * @return The file path parameter.
   */
  public String getFilePath() {
    return filePath;
  }

}
