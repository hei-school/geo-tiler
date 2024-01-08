package school.hei.geotiler.repository.model;

import static java.util.UUID.randomUUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@PrimaryKeyJoinColumn(name = "id")
@Entity
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Table(name = "zone_tiling_task_status")
public class TaskStatus extends Status {
  @JoinColumn private String taskId;

  public static TaskStatus from(String id, Status status) {
    return TaskStatus.builder()
        .taskId(id)
        .id(randomUUID().toString())
        .progression(status.getProgression())
        .health(status.getHealth())
        .creationDatetime(status.getCreationDatetime())
        .build();
  }
}
