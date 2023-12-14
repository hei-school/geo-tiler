package school.hei.geotiler.repository.model;

import static java.util.Comparator.comparing;
import static school.hei.geotiler.repository.model.types.PostgresTypes.JSONB;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.geotiler.repository.model.geo.Parcel;

@TypeDef(name = JSONB, typeClass = JsonBinaryType.class)
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties({"status"})
public class ZoneTilingTask extends ProgressiveAction<TaskStatus> implements Serializable {
  @Id private String id;

  private String jobId;
  @Getter @CreationTimestamp private Instant submissionInstant;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskId", fetch = FetchType.EAGER)
  private List<TaskStatus> statusHistory;

  @Type(type = JSONB)
  @Column(columnDefinition = JSONB)
  private Parcel parcel;

  @Override
  protected List<TaskStatus> checkStatusHistory(List<TaskStatus> statusHistory) {
    var sortedStatusHistory = new ArrayList<>(statusHistory);
    sortedStatusHistory.sort(comparing(Status::getProgression));
    sortedStatusHistory.stream()
        .reduce(
            (s1, s2) ->
                TaskStatus.builder()
                    .taskId(this.id)
                    .health(checkHealthTransition(s1.getHealth(), s2.getHealth()))
                    .progression(
                        checkProgressionTransition(s1.getProgression(), s2.getProgression()))
                    .creationDatetime(s2.getCreationDatetime())
                    .build());
    return statusHistory;
  }

  @Override
  public TaskStatus getStatus() {
    List<TaskStatus> sortedStatusHistory = new ArrayList<>(statusHistory);
    sortedStatusHistory.sort(comparing(Status::getCreationDatetime).reversed());
    return sortedStatusHistory.get(0);
  }

  @Override
  public void addStatus(TaskStatus status) {
    checkStatusTransition(this.getStatus(), status);
    this.statusHistory.add(status);
  }

  @Builder
  public ZoneTilingTask(
      String id,
      String jobId,
      Instant submissionInstant,
      List<TaskStatus> statusHistory,
      Parcel parcel) {
    this.id = id;
    this.jobId = jobId;
    this.submissionInstant = submissionInstant;
    this.statusHistory = new ArrayList<>(checkStatusHistory(statusHistory));
    this.parcel = parcel;
  }

  public List<TaskStatus> getStatusHistory() {
    if (statusHistory == null) {
      return List.of();
    }
    return statusHistory;
  }
}
