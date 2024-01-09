package school.hei.geotiler.service;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.FAILED;
import static school.hei.geotiler.repository.model.Status.HealthStatus.SUCCEEDED;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.FINISHED;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PROCESSING;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.geotiler.model.exception.NotFoundException;
import school.hei.geotiler.repository.ZoneTilingTaskRepository;
import school.hei.geotiler.repository.model.Status.HealthStatus;
import school.hei.geotiler.repository.model.Status.ProgressionStatus;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Service
@AllArgsConstructor
public class ZoneTilingTaskStatusService {
  private final ZoneTilingTaskRepository repository;
  private final ZoneTilingJobService zoneTilingJobService;
  private final EmailService emailService;

  public ZoneTilingTask process(ZoneTilingTask task) {
    return updateStatus(task, PROCESSING, UNKNOWN);
  }

  public ZoneTilingTask succeed(ZoneTilingTask task) {
    return updateStatus(task, FINISHED, SUCCEEDED);
  }

  public ZoneTilingTask fail(ZoneTilingTask task) {
    return updateStatus(task, FINISHED, FAILED);
  }

  private ZoneTilingTask updateStatus(
      ZoneTilingTask task, ProgressionStatus progression, HealthStatus health) {
    task.addStatus(
        TaskStatus.builder()
            .id(randomUUID().toString())
            .creationDatetime(now())
            .progression(progression)
            .health(health)
            .taskId(task.getId())
            .build());
    return update(task);
  }

  private ZoneTilingTask update(ZoneTilingTask zoneTilingTask) {
    if (!repository.existsById(zoneTilingTask.getId())) {
      throw new NotFoundException("ZoneTilingTask.Id = " + zoneTilingTask.getId() + " not found");
    }

    var updated = repository.save(zoneTilingTask);

    ZoneTilingJob jobRefreshedStatus = zoneTilingJobService.refreshStatus(zoneTilingTask.getJobId());

    if(jobRefreshedStatus.getStatus().getProgression() ==  FINISHED &&
        jobRefreshedStatus.getStatus().getHealth() == SUCCEEDED){
      emailService.sendEmail(zoneTilingJobService.findById(zoneTilingTask.getJobId()));
    }

    return updated;
  }
}
