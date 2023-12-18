package school.hei.geotiler.api;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


public class TilesDownloaderTest {

  @Test
  public void download_tiles_ok() {
    RestTemplate restTemplate = new RestTemplate();

    FileSystemResource server = new FileSystemResource("/run/media/dinasoa/b4c37e5f-ad8f-4388-a46b-666dc08d1a12/NUMER/HEI/geo-tiler/src/test/resources/rhone/lyon_wms.json");
    FileSystemResource geojson = new FileSystemResource("/run/media/dinasoa/b4c37e5f-ad8f-4388-a46b-666dc08d1a12/NUMER/HEI/geo-tiler/src/test/resources/rhone/departement-69-rhone-1-polygones.geojson");

    MultipartBodyBuilder bodies = new MultipartBodyBuilder();

    bodies.part("server", server);
    bodies.part("geojson", geojson);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://gft64kilv5.execute-api.eu-west-3.amazonaws.com/Prod/")
        .queryParam("zoom_size", 10);

    MultiValueMap<String, HttpEntity<?>> multipartBody = bodies.build();

    HttpEntity<MultiValueMap<String, HttpEntity<?>>> request = new HttpEntity<>(multipartBody);

    restTemplate.postForEntity(builder.toUriString(), request, byte[].class).getBody();
  }
}
