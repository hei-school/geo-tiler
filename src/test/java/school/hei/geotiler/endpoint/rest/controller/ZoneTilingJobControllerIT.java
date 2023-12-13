package school.hei.geotiler.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.geotiler.conf.FacadeIT;
import school.hei.geotiler.endpoint.event.EventProducer;
import school.hei.geotiler.endpoint.rest.model.CreateZoneTilingJob;
import school.hei.geotiler.endpoint.rest.model.Geometry;
import school.hei.geotiler.endpoint.rest.model.ZoneTilingJob;
import school.hei.geotiler.model.BoundedPageSize;
import school.hei.geotiler.model.PageFromOne;

class ZoneTilingJobControllerIT extends FacadeIT {

  @Autowired ZoneTilingController controller;
  @MockBean EventProducer eventProducer;

  @Test
  void create_tiling_job_ok() {
    CreateZoneTilingJob creatableJob =
        new CreateZoneTilingJob()
            .emailReceiver("mock@hotmail.com")
            .zoneName("Lyon")
            .geometries(List.of(new Geometry().id("geom_1_id")));

    var actual = controller.tileZone(creatableJob);
    var actualList = controller.findAll(new PageFromOne(1), new BoundedPageSize(30));

    ZoneTilingJob expectedCreated = fromCreateZoneTilingJob(creatableJob);
    assertEquals(ignoreIdOf(actual), expectedCreated);
    assertTrue(actualList.stream().map(this::ignoreIdOf).anyMatch(z -> z.equals(expectedCreated)));
  }

  private ZoneTilingJob ignoreIdOf(ZoneTilingJob zoneTilingJob) {
    return zoneTilingJob.id(null);
  }

  private ZoneTilingJob fromCreateZoneTilingJob(CreateZoneTilingJob createZoneTilingJob) {
    return new ZoneTilingJob()
        .id(null)
        .zoneName(createZoneTilingJob.getZoneName())
        .emailReceiver(createZoneTilingJob.getEmailReceiver())
        .geometries(createZoneTilingJob.getGeometries());
  }
}
