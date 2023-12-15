package school.hei.geotiler.mail;

import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.util.List;
import lombok.Builder;
import school.hei.geotiler.PojaGenerated;

@PojaGenerated
@Builder
public record Email(
    InternetAddress to,
    List<InternetAddress> cc,
    List<InternetAddress> bcc,
    String subject,
    String htmlBody,
    List<File> attachments) {}
