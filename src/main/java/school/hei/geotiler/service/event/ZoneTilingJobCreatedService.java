package school.hei.geotiler.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobCreated;
import school.hei.geotiler.service.ZoneTilingJobService;

@Service
@AllArgsConstructor
public class ZoneTilingJobCreatedService implements Consumer<ZoneTilingJobCreated> {
  private final ZoneTilingJobService zoneTilingJobService;

  @Override
  public void accept(ZoneTilingJobCreated zoneTilingJobCreated) {
    zoneTilingJobService.process(zoneTilingJobCreated.getZoneTilingJob());
  }
}
