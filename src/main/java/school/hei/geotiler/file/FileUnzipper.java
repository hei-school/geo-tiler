package school.hei.geotiler.file;

import static school.hei.geotiler.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.geotiler.model.exception.ApiException;

@Component
@AllArgsConstructor
public class FileUnzipper implements Function<ZipFile, Path> {
  private final FileWriter fileWriter;

  @Override
  public Path apply(ZipFile zipFile) {
    try {
      Path extractDirectoryPath = Files.createTempDirectory(null);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          try (InputStream is = zipFile.getInputStream(entry)) {
            byte[] bytes = is.readAllBytes();
            fileWriter.apply(bytes, extractDirectoryPath.toFile());
          }
        }
      }
      return extractDirectoryPath;
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
