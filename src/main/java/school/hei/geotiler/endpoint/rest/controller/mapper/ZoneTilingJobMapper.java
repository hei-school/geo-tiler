package school.hei.geotiler.endpoint.rest.controller.mapper;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.geotiler.endpoint.rest.model.CreateZoneTilingJob;
import school.hei.geotiler.endpoint.rest.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.JobStatus;

@Component
@AllArgsConstructor
public class ZoneTilingJobMapper {
  private final ZoneTilingTaskMapper taskMapper;
  private final GeometryMapper geometryMapper;

  public school.hei.geotiler.repository.model.ZoneTilingJob toDomain(CreateZoneTilingJob rest) {
    String generatedId = randomUUID().toString();
    return school.hei.geotiler.repository.model.ZoneTilingJob.builder()
        .id(generatedId)
        .statusHistory(
            List.of(
                JobStatus.builder()
                    .health(UNKNOWN)
                    .progression(PENDING)
                    .creationDatetime(now())
                    .jobId(generatedId)
                    .build()))
        .zoneName(rest.getZoneName())
        .emailReceiver(rest.getEmailReceiver())
        .tasks(
            rest.getGeometries().stream()
                .map(geometry -> taskMapper.fromGeometryAndJob(geometry, generatedId))
                .toList())
        .submissionInstant(now())
        .build();
  }

  public ZoneTilingJob toRest(school.hei.geotiler.repository.model.ZoneTilingJob domain) {
    return new ZoneTilingJob()
        .id(domain.getId())
        .zoneName(domain.getZoneName())
        .emailReceiver(domain.getEmailReceiver())
        .geometries(domain.getTasks().stream().map(geometryMapper::fromZoneTilingTask).toList());
  }
}
