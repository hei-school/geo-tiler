package school.hei.geotiler.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import school.hei.geotiler.model.exception.ApiException;
import school.hei.geotiler.repository.model.geo.Parcel;

@Component
public class TilesDownloaderApi {
  private final String geoTilesDownloaderApiURl = "https://gft64kilv5.execute-api.eu-west-3.amazonaws.com/Prod/";

  private final int ZOOM_SIZE = 10;

//    Request body: form-data (server: all parcel server info, geojson: parcel.feature as geojson)
//    Query param: zoom_size (int)

  public byte[] downloadTiles(Parcel parcel){
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpPost uploadFile = new HttpPost(geoTilesDownloaderApiURl);
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    builder.setContentType(ContentType.MULTIPART_FORM_DATA);


    // This attaches the file to the POST:
    File server = getServerInfoFile(parcel);
    File geojson = getGeojson(parcel);

    builder.addBinaryBody(
        "server",
        server,
        ContentType.APPLICATION_JSON,
        server.getName()
    );
    builder.addBinaryBody(
        "geojson",
        geojson,
        ContentType.APPLICATION_JSON,
        geojson.getName()
    );

    org.apache.http.HttpEntity multipart = builder.build();
    uploadFile.setEntity(multipart);
    byte[] bytes;
    try {
      CloseableHttpResponse response = httpClient.execute(uploadFile);
      org.apache.http.HttpEntity responseEntity = response.getEntity();
      bytes = responseEntity.getContent().readAllBytes();
    } catch (IOException e) {
      throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, e);
    }

    return bytes;
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

  public File getGeojson(Parcel parcel){
    Map<String, Object> geometry = new HashMap<>();
    geometry.put("type", "Polygon");
    geometry.put("coordinates", parcel.getFeature().getGeometry());

    Map<String, Object> feature = new HashMap<>();
    feature.put("type", "Feature");
    feature.put("geometry", geometry);

    List<Object> featuresList = new ArrayList<>();
    featuresList.add(feature);

    Map<String, Object> featureCollection = new HashMap<>();
    featureCollection.put("type", "FeatureCollection");
    featureCollection.put("features", featuresList);

    Path geojsonPath = Path.of("/tmp/file.geojson");

    File file = geojsonPath.toFile();
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(file, featureCollection);
      System.out.println("JSON file created successfully at: " + geojsonPath);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return file;
  }
}
