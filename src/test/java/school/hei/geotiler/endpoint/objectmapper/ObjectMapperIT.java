package school.hei.geotiler.endpoint.objectmapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.geotiler.conf.FacadeIT;

public class ObjectMapperIT extends FacadeIT {
  @Autowired ObjectMapper injectedBean;
  ObjectMapper newInstance = new ObjectMapper();

  SomeClassWithDatetimeField someClassWithDatetimeField =
      new SomeClassWithDatetimeField(Instant.now());

  @Test
  void new_instance_throws_on_java_datetime_module() {
    String jsonString = someClassWithDatetimeField.toJsonString();
    assertThrows(
        InvalidDefinitionException.class,
        () -> newInstance.readValue(jsonString, SomeClassWithDatetimeField.class));
  }

  @Test
  void injected_bean_handles_java_datetime_module() {
    assertDoesNotThrow(
        () ->
            injectedBean.readValue(
                someClassWithDatetimeField.toJsonString(), SomeClassWithDatetimeField.class));
  }
}
