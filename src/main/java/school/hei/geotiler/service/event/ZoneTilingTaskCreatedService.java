package school.hei.geotiler.service.event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingTaskCreated;
import school.hei.geotiler.file.BucketComponent;
import school.hei.geotiler.file.FileUnzipper;
import school.hei.geotiler.file.FileWriter;
import school.hei.geotiler.service.api.TilesDownloaderApi;

@Service
@AllArgsConstructor
public class ZoneTilingTaskCreatedService implements Consumer<ZoneTilingTaskCreated> {
  private final TilesDownloaderApi api;
  private final FileWriter fileWriter;
  private final FileUnzipper fileUnzipper;
  private final BucketComponent bucketComponent;

  @Override
  public void accept(ZoneTilingTaskCreated zoneTilingTaskCreated) {
    File downloadedTiles =
        fileWriter.apply(api.downloadTiles(zoneTilingTaskCreated.getTask().getGeometry()), null);
    try {
      ZipFile asZipFile = new ZipFile(downloadedTiles);
      Path unzippedPath = fileUnzipper.apply(asZipFile);
      File unzippedPathFile = unzippedPath.toFile();
      File[] files = unzippedPathFile.listFiles();
      if (files != null) {
        for (File file : files) {
          if (!file.isDirectory()) {
            bucketComponent.upload(file, file.getName());
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
