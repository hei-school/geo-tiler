package school.hei.geotiler.service.event;

import static school.hei.geotiler.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

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
import school.hei.geotiler.model.exception.ApiException;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.service.ZoneTilingTaskStatusService;
import school.hei.geotiler.service.api.TilesDownloaderApi;

@Service
@AllArgsConstructor
public class ZoneTilingTaskCreatedService implements Consumer<ZoneTilingTaskCreated> {
  private final TilesDownloaderApi tilesDownloaderApi;
  private final FileWriter fileWriter;
  private final FileUnzipper fileUnzipper;
  private final BucketComponent bucketComponent;
  private final ZoneTilingTaskStatusService zoneTilingTaskStatusService;

  @Override
  public void accept(ZoneTilingTaskCreated zoneTilingTaskCreated) {
    ZoneTilingTask task = zoneTilingTaskCreated.getTask();
    zoneTilingTaskStatusService.process(task);
    File downloadedTiles =
        fileWriter.apply(
            tilesDownloaderApi.downloadTiles(zoneTilingTaskCreated.getTask().getParcel()), null);
    try {
      ZipFile asZipFile = new ZipFile(downloadedTiles);
      String layer =
          zoneTilingTaskCreated.getTask().getParcel().getGeoServerParameter().getLayers();
      Path unzippedPath = fileUnzipper.apply(asZipFile, layer);
      File unzippedPathFile = unzippedPath.toFile();
      bucketComponent.upload(unzippedPathFile, unzippedPathFile.getName());
      zoneTilingTaskStatusService.succeed(task);
    } catch (IOException e) {
      zoneTilingTaskStatusService.fail(task);
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
