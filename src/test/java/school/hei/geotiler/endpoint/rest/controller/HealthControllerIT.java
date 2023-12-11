package school.hei.geotiler.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.geotiler.endpoint.rest.controller.HealthController.OK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.geotiler.PojaGenerated;
import school.hei.geotiler.conf.FacadeIT;

@PojaGenerated
class HealthControllerIT extends FacadeIT {

  @Autowired HealthController healthController;

  @Test
  void ping() {
    assertEquals("pong", healthController.ping());
  }

  @Test
  void can_read_from_dummy_table() {
    var responseEntity = healthController.dummyTable_should_not_be_empty();
    assertEquals(OK, responseEntity);
  }
}
