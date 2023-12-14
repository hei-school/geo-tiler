package school.hei.geotiler.repository.model.geo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import school.hei.geotiler.endpoint.rest.model.Feature;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Parcel implements Serializable {
  private String id;
  private Feature feature;
}
