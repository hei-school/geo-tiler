package school.hei.geotiler.service.api;

import org.springframework.stereotype.Component;
import school.hei.geotiler.model.exception.NotImplementedException;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Component
public class TilesDownloaderApi {
  public byte[] downloadTiles(ZoneTilingTask.Feature feature) {
    throw new NotImplementedException("Tile download from feature is not yet implemented");
  }
}
