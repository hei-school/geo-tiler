package school.hei.geotiler.service.api;

import org.springframework.stereotype.Component;
import school.hei.geotiler.model.exception.NotImplementedException;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Component
public class TilesDownloaderApi {
  public byte[] downloadTiles(ZoneTilingTask.Geometry geometry) {
    throw new NotImplementedException("Tile download from geometry is not yet implemented");
  }
}
