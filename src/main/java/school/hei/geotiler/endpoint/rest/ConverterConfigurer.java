package school.hei.geotiler.endpoint.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import school.hei.geotiler.endpoint.rest.converter.PageSizeConverter;

@Configuration
public class ConverterConfigurer implements WebMvcConfigurer {
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new PageConverter());
    registry.addConverter(new PageSizeConverter());
  }
}
