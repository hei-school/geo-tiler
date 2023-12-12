package school.hei.geotiler.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.geotiler.endpoint.rest.model.Geometry;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Component
public class GeometryMapper {
  public ZoneTilingTask.Geometry toDomain(Geometry rest) {
    return ZoneTilingTask.Geometry.builder().id(rest.getId()).build();
  }

  public Geometry fromZoneTilingTask(ZoneTilingTask domainTask) {
    return new Geometry().id(domainTask.getGeometry().getId());
  }
}
