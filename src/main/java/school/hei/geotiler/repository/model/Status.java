package school.hei.geotiler.repository.model;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static javax.persistence.GenerationType.IDENTITY;
import static school.hei.geotiler.repository.model.types.PostgresEnumType.PGSQL_ENUM_NAME;

import java.time.Instant;
import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.geotiler.repository.model.types.PostgresEnumType;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(name = PGSQL_ENUM_NAME, typeClass = PostgresEnumType.class)
public class Status {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @Enumerated(EnumType.STRING)
  @Type(type = PGSQL_ENUM_NAME)
  private ProgressionStatus progression;

  @Enumerated(EnumType.STRING)
  @Type(type = PGSQL_ENUM_NAME)
  private HealthStatus health;

  @CreationTimestamp private Instant creationDatetime;
  private String message;

  public static Status reduce(List<TaskStatus> statuses) {
    var sortedStatuses =
        statuses.stream()
            .sorted(comparing(TaskStatus::getCreationDatetime, naturalOrder()))
            .toList();
    return sortedStatuses.stream().reduce(sortedStatuses.get(0), Status::reduce);
  }

  private static TaskStatus reduce(TaskStatus oldStatus, TaskStatus newStatus) {
    var errorMessage =
        String.format("Illegal status transition: old=%s, new=%s", oldStatus, newStatus);

    var oldProgression = oldStatus.getProgression();
    var newProgression = newStatus.getProgression();
    ProgressionStatus reducedProgression =
        switch (oldProgression) {
          case PENDING -> newStatus.getProgression();
          case PROCESSING -> switch (newProgression) {
            case PENDING -> throw new IllegalArgumentException(errorMessage);
            case PROCESSING, FINISHED -> newProgression;
          };
          case FINISHED -> switch (newProgression) {
            case PENDING, PROCESSING -> throw new IllegalArgumentException(errorMessage);
            case FINISHED -> newProgression;
          };
        };

    var oldHealth = oldStatus.getHealth();
    var newHealth = newStatus.getHealth();
    HealthStatus reducedHealth =
        switch (oldHealth) {
          case UNKNOWN -> newHealth;
          case SUCCEEDED -> switch (newHealth) {
            case SUCCEEDED -> newHealth;
            case UNKNOWN, FAILED -> throw new IllegalArgumentException(errorMessage);
          };
          case FAILED -> switch (newHealth) {
            case FAILED -> newHealth;
            case UNKNOWN, SUCCEEDED -> throw new IllegalArgumentException(errorMessage);
          };
        };

    return oldProgression.equals(reducedProgression) && oldHealth.equals(reducedHealth)
        ? oldStatus
        : newStatus;
  }

  public enum ProgressionStatus {
    PENDING,
    PROCESSING,
    FINISHED
  }

  public enum HealthStatus {
    UNKNOWN,
    SUCCEEDED,
    FAILED
  }
}
