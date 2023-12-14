package school.hei.geotiler.service.event;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PROCESSING;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.geotiler.conf.FacadeIT;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobCreated;
import school.hei.geotiler.repository.ZoneTilingJobRepository;
import school.hei.geotiler.repository.model.JobStatus;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.service.ZoneTilingJobService;

class ZoneTilingJobCreatedServiceTest extends FacadeIT {
  @Autowired ZoneTilingJobCreatedService subject;
  @Autowired ZoneTilingJobService zoneTilingJobService;
  @MockBean EventProducer eventProducer;
  @Autowired ZoneTilingJobRepository repository;

  @Test
  void accept() {
    String jobId = randomUUID().toString();
    String taskId = randomUUID().toString();
    ZoneTilingJob toCreate =
        ZoneTilingJob.builder()
            .id(jobId)
            .zoneName("mock")
            .emailReceiver("mock@gmail.com")
            .tasks(
                List.of(
                    ZoneTilingTask.builder()
                        .id(taskId)
                        .jobId(jobId)
                        .submissionInstant(now())
                        .feature(
                            ZoneTilingTask.Feature.builder().id(randomUUID().toString()).build())
                        .statusHistory(
                            List.of(
                                TaskStatus.builder()
                                    .id(randomUUID().toString())
                                    .progression(PENDING)
                                    .health(UNKNOWN)
                                    .taskId(taskId)
                                    .creationDatetime(now())
                                    .build()))
                        .build()))
            .statusHistory(
                List.of(
                    JobStatus.builder()
                        .id(randomUUID().toString())
                        .jobId(jobId)
                        .progression(PENDING)
                        .health(UNKNOWN)
                        .build()))
            .build();
    ZoneTilingJob created = repository.save(toCreate);
    ZoneTilingJobCreated createdEventPayload =
        ZoneTilingJobCreated.builder().zoneTilingJob(created).build();

    subject.accept(createdEventPayload);
    ZoneTilingJob actualAfterAccept = zoneTilingJobService.findById(created.getId());

    int numberOfFeaturesInJob = 1;
    verify(eventProducer, times(numberOfFeaturesInJob)).accept(anyList());
    assertEquals(actualAfterAccept.getStatus().getHealth(), UNKNOWN);
    assertEquals(actualAfterAccept.getStatus().getProgression(), PROCESSING);
  }
}
