package school.hei.geotiler.service.event;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PROCESSING;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobCreated;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingTaskCreated;
import school.hei.geotiler.repository.model.JobStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.service.ZoneTilingJobService;

@Service
@AllArgsConstructor
public class ZoneTilingJobCreatedService implements Consumer<ZoneTilingJobCreated> {
  private final EventProducer eventProducer;
  private final ZoneTilingJobService zoneTilingJobService;

  @Override
  public void accept(ZoneTilingJobCreated zoneTilingJobCreated) {
    ZoneTilingJob job = zoneTilingJobCreated.getZoneTilingJob();
    zoneTilingJobService.updateStatus(
        job,
        JobStatus.builder()
            .id(randomUUID().toString())
            .jobId(job.getId())
            .progression(PROCESSING)
            .health(UNKNOWN)
            .creationDatetime(now())
            .build());
    // TODO: how to handle job failure ?
    fireTaskEvents(zoneTilingJobCreated.getZoneTilingJob());
  }

  private void fireTaskEvents(ZoneTilingJob zoneTilingJob) {
    zoneTilingJob.getTasks().forEach(this::fireTaskEvent);
  }

  private void fireTaskEvent(ZoneTilingTask zoneTilingTask) {
    eventProducer.accept(List.of(new ZoneTilingTaskCreated(zoneTilingTask)));
  }
}
