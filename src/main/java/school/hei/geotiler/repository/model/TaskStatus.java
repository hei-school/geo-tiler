package school.hei.geotiler.repository.model;

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
}
