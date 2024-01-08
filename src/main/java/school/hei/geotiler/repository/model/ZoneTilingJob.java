package school.hei.geotiler.repository.model;

import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static org.hibernate.annotations.FetchMode.SELECT;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;

@Entity
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"status", "done"})
public class ZoneTilingJob implements Serializable {
  @Id private String id;
  private String zoneName;
  private String emailReceiver;
  @CreationTimestamp private Instant submissionInstant;

  @OneToMany(cascade = ALL, mappedBy = "jobId", fetch = EAGER)
  @Fetch(SELECT)
  private List<JobStatus> statusHistory;

  @OneToMany(mappedBy = "jobId", cascade = ALL, fetch = EAGER)
  @Fetch(SELECT)
  private List<ZoneTilingTask> tasks = new ArrayList<>();

  public JobStatus getStatus() {
    return JobStatus.from(
        id, Status.reduce(statusHistory.stream().map(status -> (Status) status).collect(toList())));
  }

  public void addStatus(JobStatus status) {
    statusHistory.add(status);
  }
}
