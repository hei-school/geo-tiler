package school.hei.geotiler.repository.model.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.geotiler.endpoint.rest.model.MultiPolygon.TypeEnum.MULTIPOLYGON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.geotiler.conf.FacadeIT;
import school.hei.geotiler.endpoint.rest.model.Feature;
import school.hei.geotiler.endpoint.rest.model.GeoServerParameter;
import school.hei.geotiler.endpoint.rest.model.MultiPolygon;

class ParcelIT extends FacadeIT {

  @Autowired ObjectMapper om;

  @Test
  void serialize_then_deserialize() throws JsonProcessingException, MalformedURLException {
    var parcel =
        Parcel.builder()
            .geoServerUrl(new URL("https://nowhere.com"))
            .feature(new Feature().geometry(new MultiPolygon().type(MULTIPOLYGON)))
            .geoServerParameter(new GeoServerParameter().height(1024))
            .build();

    var serialized = om.writeValueAsString(parcel);
    var deserialized = om.readValue(serialized, Parcel.class);

    assertEquals(parcel.toString(), deserialized.toString());
  }
}
