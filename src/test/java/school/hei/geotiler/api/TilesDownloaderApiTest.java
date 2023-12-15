package school.hei.geotiler.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.geotiler.conf.FacadeIT;
import school.hei.geotiler.endpoint.rest.model.Feature;
import school.hei.geotiler.endpoint.rest.model.GeoServerParameter;
import school.hei.geotiler.file.BucketComponent;
import school.hei.geotiler.repository.model.geo.Parcel;
import school.hei.geotiler.service.api.TilesDownloaderApi;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;

public class TilesDownloaderApiTest extends FacadeIT {
  @MockBean BucketComponent bucketComponent;
  @Autowired TilesDownloaderApi tilesDownloaderApi;
  @Autowired ObjectMapper om;

  private Parcel parcel() throws MalformedURLException, JsonProcessingException {
    return Parcel.builder()
        .id(randomUUID().toString())
        .geoServerUrl(new URL("https://data.grandlyon.com/fr/geoserv/grandlyon/ows"))
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
        .feature(
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
                .id("feature_1_id"))
        .build();
  }

  @Test
  public void download_tiles_ok() throws MalformedURLException, JsonProcessingException {

    byte[] result = tilesDownloaderApi.downloadTiles(parcel());

    assertTrue(result.length > 0);
  }
}
