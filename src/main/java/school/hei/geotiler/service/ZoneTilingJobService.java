package school.hei.geotiler.service;

import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobCreated;
import school.hei.geotiler.model.BoundedPageSize;
import school.hei.geotiler.model.PageFromOne;
import school.hei.geotiler.repository.ZoneTilingJobRepository;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;

@Service
@AllArgsConstructor
public class ZoneTilingJobService {
  private final EventProducer eventProducer;
  private final ZoneTilingJobRepository repository;

  private void fireEvents(ZoneTilingJob job) {
    eventProducer.accept(List.of(new ZoneTilingJobCreated(job)));
  }

  public ZoneTilingJob createJob(ZoneTilingJob job) {
    boolean areAllTasksPending =
        !job.getTasks().stream()
            .allMatch(
                task -> {
                  TaskStatus status = task.getStatus();
                  return UNKNOWN.equals(status.getHealth())
                      && PENDING.equals(status.getProgression());
                });
    if (areAllTasksPending) {
      throw new RuntimeException(
          "Bad Request Exception: tasks on job creation must all have status PENDING UNKNOWN");
    }
    return repository.save(job);
  }

  public List<ZoneTilingJob> findAll(PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return repository.findAll(pageable).toList();
  }
}
