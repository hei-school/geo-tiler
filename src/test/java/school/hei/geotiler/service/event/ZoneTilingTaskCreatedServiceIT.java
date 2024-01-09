package school.hei.geotiler.service.event;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.geotiler.file.FileHashAlgorithm.SHA256;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.FINISHED;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PROCESSING;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.geotiler.conf.FacadeIT;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobStatusChanged;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingTaskCreated;
import school.hei.geotiler.endpoint.rest.model.GeoServerParameter;
import school.hei.geotiler.file.BucketComponent;
import school.hei.geotiler.file.FileHash;
import school.hei.geotiler.mail.Mailer;
import school.hei.geotiler.repository.ZoneTilingJobRepository;
import school.hei.geotiler.repository.ZoneTilingTaskRepository;
import school.hei.geotiler.repository.model.JobStatus;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.repository.model.geo.Parcel;
import school.hei.geotiler.service.api.TilesDownloaderApi;

class ZoneTilingTaskCreatedServiceIT extends FacadeIT {
  @Autowired ZoneTilingTaskCreatedService subject;
  @MockBean BucketComponent bucketComponent;
  @MockBean TilesDownloaderApi api;
  @Autowired ZoneTilingTaskRepository repository;
  @Autowired ZoneTilingJobRepository zoneTilingJobRepository;
  @MockBean EventProducer eventProducer;
  @MockBean Mailer mailer;

  @BeforeEach
  void setUp() {
    when(api.downloadTiles(any()))
        .thenAnswer(
            i -> {
              try (InputStream is =
                  this.getClass().getClassLoader().getResourceAsStream("mockData/data.zip"); ) {
                return is.readAllBytes();
              }
            });
    when(bucketComponent.upload(any(), any())).thenReturn(new FileHash(SHA256, "mock"));
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
            .parcel(
                Parcel.builder()
                    .geoServerParameter(new GeoServerParameter().layers("grand-lyon"))
                    .id(randomUUID().toString())
                    .build())
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

    int numberOfDirectoryToUpload = 1;
    verify(bucketComponent, times(numberOfDirectoryToUpload)).upload(any(), any(String.class));
  }

  @Test
  void send_statusChanged_event_on_each_status_change() {
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
            .parcel(
                Parcel.builder()
                    .id(randomUUID().toString())
                    .geoServerParameter(new GeoServerParameter().layers("grand-lyon"))
                    .build())
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

    var eventCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer, times(2)).accept(eventCaptor.capture());
    var sentEvents = eventCaptor.getAllValues().stream().flatMap(List::stream).toList();
    assertEquals(2, sentEvents.size());
    var changedToProcessing = (ZoneTilingJobStatusChanged) sentEvents.get(0);
    assertEquals(PROCESSING, changedToProcessing.getNewJob().getStatus().getProgression());
    var changedToFinished = (ZoneTilingJobStatusChanged) sentEvents.get(1);
    assertEquals(FINISHED, changedToFinished.getNewJob().getStatus().getProgression());
  }
}
