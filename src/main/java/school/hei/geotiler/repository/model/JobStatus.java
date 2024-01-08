package school.hei.geotiler.repository.model;

import static java.util.UUID.randomUUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@PrimaryKeyJoinColumn(name = "id")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Table(name = "zone_tiling_job_status")
public class JobStatus extends Status {
  @JoinColumn(referencedColumnName = "id")
  private String jobId;

  public static JobStatus from(String id, Status status) {
    return JobStatus.builder()
        .jobId(id)
        .id(randomUUID().toString())
        .progression(status.getProgression())
        .health(status.getHealth())
        .creationDatetime(status.getCreationDatetime())
        .build();
  }
}
