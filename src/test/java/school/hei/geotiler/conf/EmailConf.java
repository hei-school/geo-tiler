package school.hei.geotiler.conf;

import org.springframework.test.context.DynamicPropertyRegistry;
import school.hei.geotiler.PojaGenerated;

@PojaGenerated
public class EmailConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.ses.source", () -> "dummy-ses-source");
  }
}
