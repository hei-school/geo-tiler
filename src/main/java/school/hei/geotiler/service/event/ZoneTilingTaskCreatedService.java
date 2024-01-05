package school.hei.geotiler.service.event;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.FAILED;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PROCESSING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingTaskCreated;
import school.hei.geotiler.file.BucketComponent;
import school.hei.geotiler.file.FileUnzipper;
import school.hei.geotiler.file.FileWriter;
import school.hei.geotiler.model.exception.ApiException;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.service.ZoneTilingTaskService;
import school.hei.geotiler.service.api.TilesDownloaderApi;

@Service
@AllArgsConstructor
@Slf4j
public class ZoneTilingTaskCreatedService implements Consumer<ZoneTilingTaskCreated> {
  private final TilesDownloaderApi api;
  private final FileWriter fileWriter;
  private final FileUnzipper fileUnzipper;
  private final BucketComponent bucketComponent;
  private final ZoneTilingTaskService zoneTilingTaskService;

  @Override
  public void accept(ZoneTilingTaskCreated zoneTilingTaskCreated) {
    ZoneTilingTask task = zoneTilingTaskCreated.getTask();
    zoneTilingTaskService.updateStatus(
        task,
        TaskStatus.builder()
            .id(randomUUID().toString())
            .creationDatetime(now())
            .progression(PROCESSING)
            .health(UNKNOWN)
            .taskId(task.getId())
            .build());
    File downloadedTiles =
        fileWriter.apply(api.downloadTiles(zoneTilingTaskCreated.getTask().getParcel()), null);
    try {
      ZipFile asZipFile = new ZipFile(downloadedTiles);
      String layer =
          zoneTilingTaskCreated.getTask().getParcel().getGeoServerParameter().getLayers();
      Path unzippedPath = fileUnzipper.apply(asZipFile, layer);
      File unzippedPathFile = unzippedPath.toFile();
      File[] directories = unzippedPathFile.listFiles(File::isDirectory);
      if (directories != null) {
        for (File directory : directories) {
          File[] files = directory.listFiles();
          if (files != null) {
            for (File file : files) {
              bucketComponent.upload(file, file.getName());
            }
          }
        }
      }
      zoneTilingTaskService.finishWithSuccess(task);
    } catch (IOException e) {
      zoneTilingTaskService.updateStatus(
          task,
          TaskStatus.builder()
              .id(randomUUID().toString())
              .creationDatetime(now())
              .progression(PROCESSING)
              .health(FAILED)
              .taskId(task.getId())
              .build());
      throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, e);
    }
  }
}
