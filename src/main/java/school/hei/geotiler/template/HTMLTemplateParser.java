package school.hei.geotiler.template;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Component
public class HTMLTemplateParser implements BiFunction<String, Context, String> {
  private TemplateEngine configureEngine() {
    var templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode(HTML);

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);
    return templateEngine;
  }

  @Override
  public String apply(String template, Context context) {
    TemplateEngine engine = configureEngine();
    return engine.process(template, context);
  }
}
