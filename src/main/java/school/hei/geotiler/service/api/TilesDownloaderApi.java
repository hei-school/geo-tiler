package school.hei.geotiler.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import school.hei.geotiler.repository.model.geo.Parcel;

@Component
public class TilesDownloaderApi {
  @Autowired ObjectMapper om;
  private final String geoTilesDownloaderApiURl = "https://gft64kilv5.execute-api.eu-west-3.amazonaws.com/Prod/";
  private final String SERVER = "/tmp/serverInfo.json";
  private final String GEOJSON = "/tmp/geojson.geojson";

  public byte[] downloadTiles(Parcel parcel) {
    RestTemplate restTemplate = new RestTemplate();
    MultipartBodyBuilder bodies = new MultipartBodyBuilder();
    bodies.part("server", new FileSystemResource(getServerInfoFile(parcel)));
    bodies.part("geojson", getGeojson(parcel));
    MultiValueMap<String, HttpEntity<?>> multipartBody = bodies.build();
    HttpEntity<MultiValueMap<String, HttpEntity<?>>> request = new HttpEntity<>(multipartBody);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(geoTilesDownloaderApiURl)
        .queryParam("zoom_size", parcel.getFeature().getZoom());

    ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(builder.toUriString(), request, byte[].class);

    return responseEntity.getBody();
  }

  @SneakyThrows
  public File getServerInfoFile(Parcel parcel){
    var geoServerParameter = parcel.getGeoServerParameter();
    String geoServerUrl = String.valueOf(parcel.getGeoServerUrl());
    String service = geoServerParameter.getService();
    String request = geoServerParameter.getRequest();
    String layers = geoServerParameter.getLayers();
    String styles = geoServerParameter.getStyles();
    String format = geoServerParameter.getFormat();
    String transparent = String.valueOf(geoServerParameter.getTransparent());
    String version = geoServerParameter.getVersion();
    String width = String.valueOf(geoServerParameter.getWidth());
    String height = String.valueOf(geoServerParameter.getHeight());
    String srs = geoServerParameter.getSrs();

    Map<String, Object> serverInfo = new HashMap<>();
    serverInfo.put("url", geoServerUrl);

    Map<String, Object> serverParameter = new HashMap<>();
    serverParameter.put("service", service);
    serverParameter.put("request", request);
    serverParameter.put("layers", layers);
    serverParameter.put("styles", styles);
    serverParameter.put("format", format);
    serverParameter.put("transparent", transparent);
    serverParameter.put("version", version);
    serverParameter.put("width", width);
    serverParameter.put("height", height);
    serverParameter.put("srs", srs);

    serverInfo.put("parameter", serverParameter);
    serverInfo.put("concurrency", 1);

    Path serverInfoPath = Path.of(SERVER);
    File file = serverInfoPath.toFile();

    om.writeValue(file, serverInfo);

    return file;
  }

  @SneakyThrows
  public FileSystemResource getGeojson(Parcel parcel) {
    Map<String, Object> feature = new HashMap<>();
    feature.put("type", "Feature");
    feature.put("geometry", parcel.getFeature().getGeometry());

    List<Object> featuresList = new ArrayList<>();
    featuresList.add(feature);

    Map<String, Object> featureCollection = new HashMap<>();
    featureCollection.put("type", "FeatureCollection");
    featureCollection.put("features", featuresList);

    Path  geojsonPath = Path.of(GEOJSON);

    File geojsonFile = geojsonPath.toFile();

    om.writeValue(geojsonFile, featureCollection);

    return new FileSystemResource(geojsonFile);
  }
}
