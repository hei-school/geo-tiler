package school.hei.geotiler.service;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.SUCCEEDED;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.FINISHED;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.hei.geotiler.model.exception.NotFoundException;
import school.hei.geotiler.repository.ZoneTilingTaskRepository;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Service
@AllArgsConstructor
public class ZoneTilingTaskService {
  private final ZoneTilingTaskRepository repository;
  private final ZoneTilingJobService zoneTilingJobService;
  private final EmailService emailService;


  public ZoneTilingTask update(ZoneTilingTask zoneTilingTask) {
    if (!repository.existsById(zoneTilingTask.getId())) {
      throw new NotFoundException("ZoneTilingTask.Id = " + zoneTilingTask.getId() + " not found");
    }
    return repository.save(zoneTilingTask);
  }

  public ZoneTilingTask updateStatus(ZoneTilingTask zoneTilingTask, TaskStatus status) {
    zoneTilingTask.addStatus(status);
    return update(zoneTilingTask);
  }

  @SneakyThrows
  public ZoneTilingTask finishWithSuccess(ZoneTilingTask zoneTilingTask) {
    ZoneTilingTask finalized =
        updateStatus(
            zoneTilingTask,
            TaskStatus.builder()
                .id(randomUUID().toString())
                .creationDatetime(now())
                .progression(FINISHED)
                .health(SUCCEEDED)
                .taskId(zoneTilingTask.getId())
                .build());
    ZoneTilingJob associatedJob = zoneTilingJobService.findById(zoneTilingTask.getJobId());
    if (associatedJob.isDone()) {
      zoneTilingJobService.finishWithSuccess(associatedJob);
      emailService.sendEmail(associatedJob);
    }
    return finalized;
  }
}
