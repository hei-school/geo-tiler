package school.hei.geotiler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.geotiler.repository.model.ZoneTilingJob;

public interface ZoneTilingJobRepository extends JpaRepository<ZoneTilingJob, String> {}
