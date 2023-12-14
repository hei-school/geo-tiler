package school.hei.geotiler.service.event;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.geotiler.conf.FacadeIT;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingTaskCreated;
import school.hei.geotiler.file.BucketComponent;
import school.hei.geotiler.file.ExtensionGuesser;
import school.hei.geotiler.file.FileHash;
import school.hei.geotiler.file.FileHashAlgorithm;
import school.hei.geotiler.repository.ZoneTilingJobRepository;
import school.hei.geotiler.repository.ZoneTilingTaskRepository;
import school.hei.geotiler.repository.model.JobStatus;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.service.api.TilesDownloaderApi;

class ZoneTilingTaskCreatedServiceTest extends FacadeIT {
  @Autowired ZoneTilingTaskCreatedService subject;
  @MockBean BucketComponent bucketComponent;
  @MockBean TilesDownloaderApi api;
  @MockBean ExtensionGuesser extensionGuesser;
  @Autowired ZoneTilingTaskRepository repository;
  @Autowired ZoneTilingJobRepository zoneTilingJobRepository;
  @MockBean EventProducer eventProducer;

  @BeforeEach
  void setUp() {
    when(api.downloadTiles(any()))
        .thenAnswer(
            i -> {
              try (InputStream is =
                  this.getClass().getClassLoader().getResourceAsStream("mockData/zip.zip"); ) {
                return is.readAllBytes();
              }
            });
    when(bucketComponent.upload(any(), any()))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "mock"));
    when(extensionGuesser.apply(any())).thenCallRealMethod();
  }

  @Test
  void unzip_and_upload_ok() {
    String jobId = randomUUID().toString();
    ZoneTilingJob job =
        zoneTilingJobRepository.save(
            ZoneTilingJob.builder()
                .id(jobId)
                .statusHistory(
                    (List.of(
                        JobStatus.builder()
                            .id(randomUUID().toString())
                            .jobId(jobId)
                            .progression(PENDING)
                            .health(UNKNOWN)
                            .build())))
                .zoneName("mock")
                .emailReceiver("mock@hotmail.com")
                .build());
    String taskId = randomUUID().toString();
    ZoneTilingTask toCreate =
        ZoneTilingTask.builder()
            .id(taskId)
            .jobId(job.getId())
            .feature(ZoneTilingTask.Feature.builder().id(randomUUID().toString()).build())
            .statusHistory(
                List.of(
                    TaskStatus.builder()
                        .id(randomUUID().toString())
                        .taskId(taskId)
                        .progression(PENDING)
                        .health(UNKNOWN)
                        .build()))
            .build();
    ZoneTilingTask created = repository.save(toCreate);
    ZoneTilingTaskCreated createdEventPayload =
        ZoneTilingTaskCreated.builder().task(created).build();

    subject.accept(createdEventPayload);

    int numberOfZipFiles = 1;
    int numberOfNotDirectoryFilesInZip = 2;
    verify(extensionGuesser, times(numberOfZipFiles + numberOfNotDirectoryFilesInZip)).apply(any());
    verify(bucketComponent, times(numberOfNotDirectoryFilesInZip)).upload(any(), any(String.class));
  }
}
;
