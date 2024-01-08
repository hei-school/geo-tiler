package school.hei.geotiler.repository.model;

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
    return null; // TODO
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
