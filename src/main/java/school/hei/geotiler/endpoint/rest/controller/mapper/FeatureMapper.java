package school.hei.geotiler.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.geotiler.endpoint.rest.model.Feature;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.repository.model.geo.Parcel;

@Component
public class FeatureMapper {
  public Parcel toDomain(Feature rest) {
    return Parcel.builder().id(rest.getId()).feature(rest).build();
  }

  public Feature fromZoneTilingTask(ZoneTilingTask domainTask) {
    return domainTask.getParcel().getFeature();
  }
}
