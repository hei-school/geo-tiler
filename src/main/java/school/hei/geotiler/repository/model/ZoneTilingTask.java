package school.hei.geotiler.repository.model;

import static java.util.stream.Collectors.toList;
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
import lombok.AllArgsConstructor;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties({"status"})
public class ZoneTilingTask implements Serializable {
  @Id private String id;

  private String jobId;
  @Getter @CreationTimestamp private Instant submissionInstant;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskId", fetch = FetchType.EAGER)
  private List<TaskStatus> statusHistory = new ArrayList<>();

  @Type(type = JSONB)
  @Column(columnDefinition = JSONB)
  private Parcel parcel;

  public TaskStatus getStatus() {
    return TaskStatus.from(
        id, Status.reduce(statusHistory.stream().map(status -> (Status) status).collect(toList())));
  }

  public void addStatus(TaskStatus status) {
    statusHistory.add(status);
  }
}
