package school.hei.geotiler.endpoint.rest;

import org.springframework.core.convert.converter.Converter;
import school.hei.geotiler.model.PageFromOne;

public class PageConverter implements Converter<String, PageFromOne> {
  @Override
  public PageFromOne convert(String source) {
    return new PageFromOne(Integer.parseInt(source));
  }
}
