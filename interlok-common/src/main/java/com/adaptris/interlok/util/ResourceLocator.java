package com.adaptris.interlok.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public abstract class ResourceLocator {

  enum LocalResource {
    Filesystem() {

      @Override
      boolean canFind(String path) {
        return new File(path).exists();
      }

      @Override
      URL resolve(String path) throws MalformedURLException {
        return new File(path).toURI().toURL();
      }

    },
    Classpath() {

      @Override
      boolean canFind(String path) {
        return ResourceLocator.class.getClassLoader().getResource(path) != null;
      }

      @Override
      URL resolve(String path) {
        return ResourceLocator.class.getClassLoader().getResource(path);
      }
    },
    DefaultBehaviour() {
      @Override
      boolean canFind(String path) {
        return true;
      }

      @Override
      URL resolve(String path) throws MalformedURLException, URISyntaxException {
        // is this a sensible default for a non-absolute URI -> might be "/xyz" "./xyz"
        // which might not exist; which means that we end up returning a somewhat
        // duff file URL which won't resolve anyway, so it probably doesn't
        // matter
        return new URL("file:///" + new URI(null, path, null).toASCIIString());
      }

    };

    abstract boolean canFind(String path);

    abstract URL resolve(String path) throws Exception;
  }

  private static final List<LocalResource> RESOLVERS =
      Arrays.asList(LocalResource.Filesystem, LocalResource.Classpath, LocalResource.DefaultBehaviour);

  public static URL toURL(String s) throws Exception {
    URI configuredUri = toURI(s);
    return !configuredUri.isAbsolute() ? localResource(configuredUri) : new URL(configuredUri.toString());
  }


  private static URI toURI(String s) throws Exception {
    String destToConvert = backslashToSlash(Args.notNull(s, "uri"));
    URI configuredUri = null;
    try {
      configuredUri = new URI(destToConvert);
    } catch (URISyntaxException e) {
      if (destToConvert.split(":").length >= 3) {
        configuredUri = new URI(URLEncoder.encode(destToConvert, "UTF-8"));
      } else {
        throw e;
      }
    }
    return configuredUri;
  }

  private static URL localResource(URI uri) throws Exception {
    final String path = uri.getPath();
    return RESOLVERS.stream().filter((r) -> r.canFind(path)).findFirst().get().resolve(path);
  }

  private static String backslashToSlash(String url) {
    return url.replace('\\', '/');
  }

}
