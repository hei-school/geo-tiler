package school.hei.geotiler.service.event;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingJobCreated;
import school.hei.geotiler.endpoint.event.gen.ZoneTilingTaskCreated;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;

@Service
@AllArgsConstructor
public class ZoneTilingJobCreatedService implements Consumer<ZoneTilingJobCreated> {
  private final EventProducer eventProducer;

  @Override
  public void accept(ZoneTilingJobCreated zoneTilingJobCreated) {
    fireTaskEvents(zoneTilingJobCreated.getZoneTilingJob());
  }

  private void fireTaskEvents(ZoneTilingJob zoneTilingJob) {
    zoneTilingJob.getTasks().forEach(this::fireTaskEvent);
  }

  private void fireTaskEvent(ZoneTilingTask zoneTilingTask) {
    eventProducer.accept(List.of(new ZoneTilingTaskCreated(zoneTilingTask)));
  }
}
