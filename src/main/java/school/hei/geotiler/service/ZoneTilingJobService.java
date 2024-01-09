package school.hei.geotiler.service;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PROCESSING;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobCreated;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobStatusChanged;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingTaskCreated;
import school.hei.geotiler.model.BoundedPageSize;
import school.hei.geotiler.model.PageFromOne;
import school.hei.geotiler.model.exception.NotFoundException;
import school.hei.geotiler.repository.ZoneTilingJobRepository;
import school.hei.geotiler.repository.model.JobStatus;
import school.hei.geotiler.repository.model.Status;
import school.hei.geotiler.repository.model.Status.HealthStatus;
import school.hei.geotiler.repository.model.Status.ProgressionStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Service
@AllArgsConstructor
public class ZoneTilingJobService {
  private final EventProducer eventProducer;
  private final ZoneTilingJobRepository repository;

  private void fireEvents(ZoneTilingJob job) {
    eventProducer.accept(List.of(new ZoneTilingJobCreated(job)));
  }

  public ZoneTilingJob create(ZoneTilingJob job) {
    if (!areAllTasksPending(job)) {
      throw new IllegalArgumentException(
          "Tasks on job creation must all have status PENDING");
    }

    var saved = repository.save(job);
    fireEvents(saved);
    return saved;
  }

  private static boolean areAllTasksPending(ZoneTilingJob job) {
    return job.getTasks().stream()
        .map(ZoneTilingTask::getStatus)
        .allMatch(status -> PENDING.equals(status.getProgression()));
  }

  public List<ZoneTilingJob> findAll(PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return repository.findAll(pageable).toList();
  }

  public ZoneTilingJob findById(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("ZoneTilingJob.Id " + id + " not found"));
  }

  public ZoneTilingJob process(ZoneTilingJob job) {
    var processed = updateStatus(job, PROCESSING, UNKNOWN);
    job.getTasks().forEach(task -> eventProducer.accept(List.of(new ZoneTilingTaskCreated(task))));
    return processed;
  }

  private ZoneTilingJob updateStatus(
      ZoneTilingJob job, ProgressionStatus progression, HealthStatus health) {
    job.addStatus(
        JobStatus.builder()
            .id(randomUUID().toString())
            .jobId(job.getId())
            .progression(progression)
            .health(health)
            .creationDatetime(now())
            .build());
    return repository.save(job);
  }

  private ZoneTilingJob updateStatus(ZoneTilingJob job, Status status) {
    job.addStatus(JobStatus.from(randomUUID().toString(), status));
    return repository.save(job);
  }

  public ZoneTilingJob refreshStatus(String jobId) {
    var oldJob = findById(jobId);
    Status oldStatus = oldJob.getStatus();
    Status newStatus =
        Status.reduce(
            oldJob.getTasks().stream()
                .map(ZoneTilingTask::getStatus)
                .map(status -> (Status) status)
                .toList());
    if (oldStatus.equals(newStatus)) {
      return oldJob;
    }

    var refreshed = updateStatus(oldJob, newStatus);
    eventProducer.accept(
        List.of(ZoneTilingJobStatusChanged.builder().oldJob(oldJob).newJob(refreshed).build()));
    return refreshed;
  }
}
