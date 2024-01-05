package school.hei.geotiler.file;

import static java.util.UUID.randomUUID;
import static school.hei.geotiler.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.geotiler.model.exception.ApiException;

@Component
@AllArgsConstructor
public class FileWriter implements BiFunction<byte[], File, File> {
  private final ExtensionGuesser extensionGuesser;

  @Override
  public File apply(byte[] bytes, @Nullable File directory) {
    try {
      String name = randomUUID().toString();
      String suffix = "." + extensionGuesser.apply(bytes);
      File tempFile = File.createTempFile(name, suffix, directory);
      return Files.write(tempFile.toPath(), bytes).toFile();
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public File write(byte[] bytes, @Nullable File directory, String filename) {
    try {
      String suffix = extensionGuesser.apply(bytes);
      File newFile = new File(directory, filename + suffix);
      Files.createDirectories(newFile.toPath().getParent());
      return Files.write(newFile.toPath(), bytes).toFile();
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
