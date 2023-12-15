package school.hei.geotiler.utils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class HTMLUtils {
  public static String parseToString(String template, Context context) {
    TemplateEngine engine = configureEngine();
    return engine.process(template, context);
  }

  private static TemplateEngine configureEngine() {
    var templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode(TemplateMode.HTML);

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);
    return templateEngine;
  }
}
