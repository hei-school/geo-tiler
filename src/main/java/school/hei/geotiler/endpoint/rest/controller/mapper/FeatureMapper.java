package school.hei.geotiler.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.geotiler.endpoint.rest.model.Feature;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Component
public class FeatureMapper {
  public ZoneTilingTask.Feature toDomain(Feature rest) {
    return ZoneTilingTask.Feature.builder().id(rest.getId()).build();
  }

  public Feature fromZoneTilingTask(ZoneTilingTask domainTask) {
    return new Feature().id(domainTask.getFeature().getId());
  }
}
