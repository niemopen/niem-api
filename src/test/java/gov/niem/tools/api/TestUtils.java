package gov.niem.tools.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import gov.niem.tools.api.core.utils.FileUtils;

public class TestUtils {

  public static String getResourcesFileText(String path) throws IOException {
    return IOUtils.resourceToString("/" + path, StandardCharsets.UTF_8);
  }

  public static URL getResourcesFileUri(String path) throws IOException {
    return IOUtils.resourceToURL("/" + path);
  }

  public static File getResourcesFile(String path) throws IOException {
    URL url = TestUtils.getResourcesFileUri(path);
    return FileUtils.file(url.getPath());
  }

  public static MultipartFile getMultipartFile(String path) throws IOException, URISyntaxException {
    URL url = IOUtils.resourceToURL("/" + path);
    File file = new File(url.toURI());
    FileInputStream inputStream = new FileInputStream(file);
    String contentType = Files.probeContentType(file.toPath());
    return new MockMultipartFile(file.getName(), file.getName(), contentType, inputStream);
  }

}
