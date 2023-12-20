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

  private final int ZOOM_SIZE = 10;

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
        .queryParam("zoom_size", ZOOM_SIZE);

    MultiValueMap<String, HttpEntity<?>> multipartBody = bodies.build();

    HttpEntity<MultiValueMap<String, HttpEntity<?>>> request = new HttpEntity<>(multipartBody);

    return restTemplate.postForEntity(builder.toUriString(), request, byte[].class).getBody();
  }


  public File getServerInfoFile(Parcel parcel){

    String geoServerUrl = String.valueOf(parcel.getGeoServerUrl());
    String service = parcel.getGeoServerParameter().getService();
    String request = parcel.getGeoServerParameter().getRequest();
    String layers = parcel.getGeoServerParameter().getLayers();
    String styles = parcel.getGeoServerParameter().getStyles();
    String format = parcel.getGeoServerParameter().getFormat();
    String transparent = String.valueOf(parcel.getGeoServerParameter().getTransparent());
    String version = parcel.getGeoServerParameter().getVersion();
    String width = String.valueOf(parcel.getGeoServerParameter().getWidth());
    String height = String.valueOf(parcel.getGeoServerParameter().getHeight());
    String srs = parcel.getGeoServerParameter().getSrs();


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
    serverInfo.put("concurrency", 8);

    Path serverInfoPath = Path.of("/tmp/serverInfo.json");

    File file = serverInfoPath.toFile();

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(file, serverInfo);
      System.out.println("JSON file created successfully at: " + serverInfoPath);
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

    Path geojsonPath = Path.of("/tmp/geojsonPath.geojson");

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
