package school.hei.geotiler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.geotiler.repository.model.ZoneTilingTask;

public interface ZoneTilingTaskRepository extends JpaRepository<ZoneTilingTask, String> {}
