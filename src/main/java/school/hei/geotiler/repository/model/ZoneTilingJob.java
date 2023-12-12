package school.hei.geotiler.repository.model;

import static java.util.Comparator.comparing;

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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@NoArgsConstructor
public class ZoneTilingJob extends ProgressiveAction<JobStatus> {
  @Id private String id;
  private String zoneName;
  private String emailReceiver;
  @CreationTimestamp private Instant submissionInstant;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobId")
  private List<JobStatus> statusHistory;

  @OneToMany(mappedBy = "jobId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<ZoneTilingTask> tasks;

  @Override
  public List<JobStatus> checkStatusHistory(List<JobStatus> statusHistory) {
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
    this.statusHistory = checkStatusHistory(statusHistory);
    this.tasks = tasks;
  }
}
