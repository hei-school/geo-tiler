package school.hei.geotiler.service.api;

import static school.hei.geotiler.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import school.hei.geotiler.endpoint.rest.model.GeoServerParameter;
import school.hei.geotiler.model.exception.ApiException;
import school.hei.geotiler.repository.model.geo.Parcel;

@Component
@Slf4j
@AllArgsConstructor
public class TilesDownloaderApi {
    private static final URI geoTilesDownloaderApiURl;
    private static final int AWS_LAMBDA_SERVER_SUPPORTED_CONCURRENCY_NUMBER = 1;
    private final ObjectMapper om;

    static {
        try {
            geoTilesDownloaderApiURl = new URI("https://gft64kilv5.execute-api.eu-west-3.amazonaws.com/Prod/?zoom_size=1");
        } catch (URISyntaxException e) {
            throw new ApiException(SERVER_EXCEPTION, e);
        }
    }

    @SneakyThrows
    public byte[] downloadTiles(Parcel parcel) {
        RestTemplate restTemplate = new RestTemplate();
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

        multipartBodyBuilder.part("server", getServerInfoFile(parcel));
        multipartBodyBuilder.part("geojson", getServerInfoFile(parcel));
        MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();
        HttpEntity<?> requestEntity
                = new HttpEntity<>(multipartBody);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(geoTilesDownloaderApiURl, requestEntity, byte[].class);

        if (response.getStatusCode().isError()) {
            throw new ApiException(SERVER_EXCEPTION, "Error on POST : " + geoTilesDownloaderApiURl + " " + response.getStatusCode().getReasonPhrase());
        }
        return response.getBody();
    }

    private FileSystemResource getServerInfoFile(Parcel parcel) {
        String geoServerUrl = String.valueOf(parcel.getGeoServerUrl());
        var geoServerParameter = parcel.getGeoServerParameter();
        Map<String, Object> serverInfo = getServerInfo(geoServerUrl, geoServerParameter);
        Path serverInfoPath = Path.of("/tmp/serverInfo.json");
        File file = serverInfoPath.toFile();
        try {
           om.writeValue(file, serverInfo);
        } catch (IOException e) {
            throw new ApiException(SERVER_EXCEPTION, e);
        }
        return new FileSystemResource(file);
    }

    private static Map<String, Object> getServerInfo(
            String geoServerUrl,
            GeoServerParameter geoServerParameter) {
        Map<String, Object> serverParameter = Map.of(
                "service", geoServerParameter.getService(),
                "request", geoServerParameter.getRequest(),
                "layers", geoServerParameter.getStyles(),
                "styles", geoServerParameter.getStyles(),
                "format", geoServerParameter.getFormat(),
                "transparent", geoServerParameter.getTransparent(),
                "version", geoServerParameter.getVersion(),
                "width", geoServerParameter.getWidth(),
                "height", geoServerParameter.getHeight(),
                "srs", geoServerParameter.getSrs()
        );
        Map<String, Object> serverInfo = Map.of(
                "url", geoServerUrl,
                "parameter", serverParameter,
                "concurrency", 1
        );
        return serverInfo;
    }

    private FileSystemResource getGeojson(Parcel parcel) {
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

        Path geojsonPath = Path.of("/tmp/geojsonPath.geojson");


        File geojsonFile = geojsonPath.toFile();

        try {
            om.writeValue(geojsonFile, featureCollection);
        } catch (IOException e) {
            throw new ApiException(SERVER_EXCEPTION, e);
        }

        return new FileSystemResource(geojsonFile);
    }
}
