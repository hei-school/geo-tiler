package school.hei.geotiler.file;

import static school.hei.geotiler.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.geotiler.model.exception.ApiException;

@Component
@AllArgsConstructor
@Slf4j
public class FileUnzipper implements BiFunction<ZipFile, String, Path> {
  private final FileWriter fileWriter;

  @Override
  public Path apply(ZipFile zipFile, String mainDir) {
    try {
      Path extractDirectoryPath = Files.createTempDirectory(mainDir);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String entryName = entry.getName();
        String subDir = entryName.split("/")[0];
        String zipPrefix = subDir + "/";

        if (!entry.isDirectory() && entryName.startsWith(zipPrefix)) {
          try (InputStream is = zipFile.getInputStream(entry)) {
            byte[] bytes = is.readAllBytes();
            String relativePath = entryName.substring(zipPrefix.length());
            String[] pathSegments = relativePath.split("/");

            if (pathSegments.length >= 2) {
              String folderName = pathSegments[0];
              String fileNameWithPrefix = pathSegments[1];
              String fileName = fileNameWithPrefix.substring(0, fileNameWithPrefix.lastIndexOf("."));
              Path destinationPath = extractDirectoryPath.resolve(subDir);
              String fileNameReferenced = folderName + "_" + fileName;
              fileWriter.write(bytes, destinationPath.toFile(), fileNameReferenced);
            }
          }
        }
      }

      return extractDirectoryPath;
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
