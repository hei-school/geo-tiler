package school.hei.geotiler.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import school.hei.geotiler.model.exception.ApiException;
import school.hei.geotiler.repository.model.geo.Parcel;

import static school.hei.geotiler.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
@Slf4j
public class TilesDownloaderApi {

  @Autowired ObjectMapper om;

  private final String geoTilesDownloaderApiURl = "https://gft64kilv5.execute-api.eu-west-3.amazonaws.com/Prod/";
  private final String SERVER = "/tmp/serverInfo.json";
  private final String GEOJSON = "/tmp/geojson.geojson";

//    Request body: form-data (server: all parcel server info, geojson: parcel.feature as geojson)
//    Query param: zoom_size (int)

  public byte[] downloadTiles(Parcel parcel) throws JsonProcessingException {
    RestTemplate restTemplate = new RestTemplate();

    FileSystemResource server = new FileSystemResource(getServerInfoFile(parcel));
    FileSystemResource geojson = getGeojson(parcel);

    MultipartBodyBuilder bodies = new MultipartBodyBuilder();

    bodies.part("server", server);
    bodies.part("geojson", geojson);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(geoTilesDownloaderApiURl)
        .queryParam("zoom_size", parcel.getFeature().getZoom());

    MultiValueMap<String, HttpEntity<?>> multipartBody = bodies.build();

    HttpEntity<MultiValueMap<String, HttpEntity<?>>> request = new HttpEntity<>(multipartBody);

    return restTemplate.postForEntity(builder.toUriString(), request, byte[].class).getBody();
  }


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

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(file, serverInfo);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file;
  }

//  {"type": "Polygon", "type": "Feature", "geometry":{"type":"Polygon", "coordinates": []}, "type":"FeatureCollection", "features":[]}
  public FileSystemResource getGeojson(Parcel parcel) throws JsonProcessingException {
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
    log.info("GEOJSON: {}", om.writeValueAsString(featureCollection));
    try {
      om.writeValue(geojsonFile, featureCollection);
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }

    return new FileSystemResource(geojsonFile);
  }
}
