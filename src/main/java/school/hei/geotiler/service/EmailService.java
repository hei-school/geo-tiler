package school.hei.geotiler.service;

import jakarta.mail.internet.InternetAddress;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.geotiler.mail.Email;
import school.hei.geotiler.mail.Mailer;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.utils.HTMLTemplateParser;

@Service
@AllArgsConstructor
public class EmailService {
  private final Mailer mailer;
  public static final String EMAIL_OBJECT = "Résultat du pavage de la zone";
  public static final String ZONE_TILING_TEMPLATE_NAME = "zone_tiling";
  private HTMLTemplateParser htmlTemplateParser;

  @SneakyThrows
  public void sendEmail(ZoneTilingJob zoneTilingJob) {
    Context context = new Context();
    context.setVariable("zone", zoneTilingJob.getZoneName());
    String emailBody = htmlTemplateParser.apply(ZONE_TILING_TEMPLATE_NAME, context);

    mailer.accept(
        new Email(
            new InternetAddress(zoneTilingJob.getEmailReceiver()),
            List.of(),
            List.of(),
            EMAIL_OBJECT,
            emailBody,
            List.of()));
  }
}
