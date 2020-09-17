package com.adaptris.interlok.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
    FileUrl() {

      @Override
      boolean canFind(String path) {
        return new File(stripRelativeFileUrl(path)).exists();
      }

      @Override
      URL resolve(String path) throws MalformedURLException {
        return new File(stripRelativeFileUrl(path)).toURI().toURL();
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
      Arrays.asList(LocalResource.Filesystem, LocalResource.FileUrl, LocalResource.Classpath,
          LocalResource.DefaultBehaviour);

  public static URL toURL(String s) throws Exception {
    URI configuredUri = toURI(s);
    return isLocal(configuredUri) ? localResource(configuredUri)
        : new URL(configuredUri.toString());
  }

  private static boolean isLocal(URI uri) throws Exception {
    URI uriToParse = Args.notNull(uri, "uri");
    String scheme = StringUtils.defaultIfEmpty(uriToParse.getScheme(), "");
    return BooleanUtils.or(new boolean[] {
        StringUtils.isBlank(scheme),
        scheme.toLowerCase().startsWith("file")
    });
  }


  private static URI toURI(String s) throws Exception {
    String destToConvert = backslashToSlash(Args.notNull(s, "uri"));
    return new URI(destToConvert);
  }

  private static URL localResource(URI uri) throws Exception {
    final String path = uri.getPath();
    return RESOLVERS.stream().filter((r) -> r.canFind(path)).findFirst().get().resolve(path);
  }

  private static String backslashToSlash(String url) {
    return url.replace('\\', '/');
  }

  private static String stripRelativeFileUrl(String filepath) {
    String filename = filepath;
    if (filename.startsWith("/.")) {
      filename = filename.substring(1);
    }
    return filename;
  }
}
