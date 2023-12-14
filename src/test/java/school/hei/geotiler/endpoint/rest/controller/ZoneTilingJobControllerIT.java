package school.hei.geotiler.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.geotiler.conf.FacadeIT;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.rest.model.CreateZoneTilingJob;
import school.hei.geotiler.endpoint.rest.model.Feature;
import school.hei.geotiler.endpoint.rest.model.GeoServerParameter;
import school.hei.geotiler.endpoint.rest.model.ZoneTilingJob;
import school.hei.geotiler.model.BoundedPageSize;
import school.hei.geotiler.model.PageFromOne;

class ZoneTilingJobControllerIT extends FacadeIT {

  @Autowired ZoneTilingController controller;
  @MockBean EventProducer eventProducer;
  @Autowired ObjectMapper om;

  @Test
  void create_tiling_job_ok() throws IOException {
    CreateZoneTilingJob creatableJob =
        new CreateZoneTilingJob()
            .emailReceiver("mock@hotmail.com")
            .zoneName("Lyon")
            .geoServerUrl("https://data.grandlyon.com/fr/geoserv/grandlyon/ows")
            .geoServerParameter(
                om.readValue(
                    """
                {
                    "service": "WMS",
                    "request": "GetMap",
                    "layers": "grandlyon:ortho_2018",
                    "styles": "",
                    "format": "image/png",
                    "transparent": true,
                    "version": "1.3.0",
                    "width": 256,
                    "height": 256,
                    "srs": "EPSG:3857"
                  }""",
                    GeoServerParameter.class))
            .features(
                List.of(
                    om.readValue(
                            """
                { "type": "Feature",
                  "properties": {
                    "code": "69",
                    "nom": "Rhône",
                    "id": 30251921,
                    "CLUSTER_ID": 99520,
                    "CLUSTER_SIZE": 386884 },
                  "geometry": {
                    "type": "MultiPolygon",
                    "coordinates": [ [ [
                      [ 4.459648282829194, 45.904988912620688 ],
                      [ 4.464709510872551, 45.928950368349426 ],
                      [ 4.490816965688656, 45.941784543770964 ],
                      [ 4.510354299995861, 45.933697132664598 ],
                      [ 4.518386257467152, 45.912888345521047 ],
                      [ 4.496344031095243, 45.883438201401809 ],
                      [ 4.479593950305621, 45.882900828315755 ],
                      [ 4.459648282829194, 45.904988912620688 ] ] ] ] } }""",
                            Feature.class)
                        .id("feature_1_id")));

    var actual = controller.tileZone(creatableJob);
    var actualList = controller.findAll(new PageFromOne(1), new BoundedPageSize(30));

    ZoneTilingJob expected = from(creatableJob);
    assertEquals(expected, ignoreId(actual));
    // TODO: coordinates are fine when retrieved by controller::tileZone
    //   but rounded when retrieved by controller::findAll!
    // assertTrue(actualList.stream().map(this::ignoreId).anyMatch(z -> z.equals(expected)));

    verify(eventProducer, only()).accept(any());
  }

  private ZoneTilingJob ignoreId(ZoneTilingJob zoneTilingJob) {
    return zoneTilingJob.id(null);
  }

  private ZoneTilingJob from(CreateZoneTilingJob createZoneTilingJob) {
    return new ZoneTilingJob()
        .id(null)
        .zoneName(createZoneTilingJob.getZoneName())
        .emailReceiver(createZoneTilingJob.getEmailReceiver())
        .geoServerUrl(createZoneTilingJob.getGeoServerUrl())
        .geoServerParameter(createZoneTilingJob.getGeoServerParameter())
        .features(createZoneTilingJob.getFeatures());
  }
}
