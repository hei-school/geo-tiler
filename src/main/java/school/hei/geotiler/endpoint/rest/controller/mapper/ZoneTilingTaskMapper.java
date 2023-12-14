package school.hei.geotiler.endpoint.rest.controller.mapper;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.UNKNOWN;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.PENDING;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.geotiler.endpoint.rest.model.Feature;
import school.hei.geotiler.endpoint.rest.model.GeoServerParameter;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Component
@AllArgsConstructor
public class ZoneTilingTaskMapper {
  private final FeatureMapper featureMapper;

  public ZoneTilingTask from(
      Feature createFeature, URL geoServerUrl, GeoServerParameter geoServerParameter, UUID jobId) {
    String generatedId = randomUUID().toString();
    return ZoneTilingTask.builder()
        .id(generatedId)
        .jobId(jobId.toString())
        .statusHistory(
            List.of(
                TaskStatus.builder()
                    .health(UNKNOWN)
                    .progression(PENDING)
                    .creationDatetime(now())
                    .taskId(generatedId)
                    .build()))
        .submissionInstant(now())
        .parcel(featureMapper.toDomain(createFeature, geoServerUrl, geoServerParameter))
        .build();
  }
}
