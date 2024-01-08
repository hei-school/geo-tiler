package school.hei.geotiler.file;

import static school.hei.geotiler.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.geotiler.model.exception.ApiException;

@Component
@AllArgsConstructor
public class FileUnzipper implements BiFunction<ZipFile, String, Path> {
  private final FileWriter fileWriter;

  @Override
  public Path apply(ZipFile zipFile, String mainDir) {
    try {
      Path extractDirectoryPath = Files.createTempDirectory(mainDir);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          try (InputStream is = zipFile.getInputStream(entry)) {
            String entryParentPath = getFolderPath(entry);
            String entryFilename = getFilename(entry);
            String extensionlessEntryFilename = stripExtension(entryFilename);
            Path destinationPath = extractDirectoryPath.resolve(entryParentPath);
            byte[] bytes = is.readAllBytes();
            fileWriter.write(bytes, destinationPath.toFile(), extensionlessEntryFilename);
          }
        }
      }

      return extractDirectoryPath;
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public String getFolderPath(ZipEntry zipEntry) {
    String entryPath = zipEntry.getName();
    return entryPath.substring(0, entryPath.lastIndexOf("/"));
  }

  public String getFilename(ZipEntry zipEntry) {
    String entryPath = zipEntry.getName();
    return Paths.get(entryPath).getFileName().toString();
  }

  public String stripExtension(String filename) {
    return filename.substring(0, filename.lastIndexOf("."));
  }
}
