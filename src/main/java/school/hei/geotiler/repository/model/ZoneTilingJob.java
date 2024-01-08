package school.hei.geotiler.repository.model;

import static java.util.Comparator.comparing;
import static school.hei.geotiler.repository.model.Status.HealthStatus.SUCCEEDED;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.FINISHED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"status", "done"})
public class ZoneTilingJob extends ProgressiveAction<JobStatus> {
  @Id private String id;
  private String zoneName;
  private String emailReceiver;
  @CreationTimestamp private Instant submissionInstant;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobId", fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
  private List<JobStatus> statusHistory;

  @OneToMany(mappedBy = "jobId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
  private List<ZoneTilingTask> tasks;

  @Override
  protected List<JobStatus> checkStatusHistory(List<JobStatus> statusHistory) {
    var sortedStatusHistory = new ArrayList<>(statusHistory);
    sortedStatusHistory.sort(comparing(Status::getProgression));
    sortedStatusHistory.stream()
        .reduce(
            (s1, s2) ->
                JobStatus.builder()
                    .jobId(this.id)
                    .health(checkHealthTransition(s1.getHealth(), s2.getHealth()))
                    .progression(
                        checkProgressionTransition(s1.getProgression(), s2.getProgression()))
                    .creationDatetime(s2.getCreationDatetime())
                    .build());
    return statusHistory;
  }

  @Override
  public JobStatus getStatus() {
    List<JobStatus> sortedStatusHistory = new ArrayList<>(statusHistory);
    sortedStatusHistory.sort(comparing(Status::getCreationDatetime).reversed());
    return sortedStatusHistory.get(0);
  }

  @Override
  public void addStatus(JobStatus status) {
    checkStatusTransition(this.getStatus(), status);
    this.statusHistory.add(status);
  }

  @Builder
  public ZoneTilingJob(
      String id,
      String zoneName,
      String emailReceiver,
      Instant submissionInstant,
      List<JobStatus> statusHistory,
      List<ZoneTilingTask> tasks) {
    this.id = id;
    this.zoneName = zoneName;
    this.emailReceiver = emailReceiver;
    this.submissionInstant = submissionInstant;
    this.statusHistory = new ArrayList<>(checkStatusHistory(statusHistory));
    this.tasks = tasks;
  }

  public boolean succeeded() {
    return this.getTasks().stream()
        .allMatch(
            task -> {
              TaskStatus status = task.getStatus();
              return FINISHED.equals(status.getProgression())
                  && SUCCEEDED.equals(status.getHealth());
            });
  }

  public List<ZoneTilingTask> getTasks() {
    if (tasks == null) {
      return List.of();
    }
    return tasks;
  }
}
