package school.hei.geotiler.endpoint.rest.controller.mapper;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.geotiler.endpoint.rest.model.Geometry;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Component
@AllArgsConstructor
public class ZoneTilingTaskMapper {
  private final GeometryMapper geometryMapper;

  public ZoneTilingTask fromGeometryAndJob(Geometry geometry, String jobId) {
    String generatedId = randomUUID().toString();
    return ZoneTilingTask.builder()
        .id(generatedId)
        .jobId(jobId)
        .statusHistory(
            List.of(
                TaskStatus.builder()
                    .health(UNKNOWN)
                    .progression(PENDING)
                    .creationDatetime(now())
                    .taskId(generatedId)
                    .build()))
        .submissionInstant(now())
        .geometry(geometryMapper.toDomain(geometry))
        .build();
  }
}
