package school.hei.geotiler.service;

import static java.io.File.createTempFile;
import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.geotiler.repository.model.Status.HealthStatus.SUCCEEDED;
import static school.hei.geotiler.repository.model.Status.ProgressionStatus.FINISHED;


import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.geotiler.mail.Email;
import school.hei.geotiler.mail.Mailer;
import school.hei.geotiler.model.exception.ApiException;
import school.hei.geotiler.model.exception.NotFoundException;
import school.hei.geotiler.repository.ZoneTilingTaskRepository;
import school.hei.geotiler.repository.model.TaskStatus;
import school.hei.geotiler.repository.model.ZoneTilingJob;
import school.hei.geotiler.repository.model.ZoneTilingTask;
import school.hei.geotiler.utils.HTMLUtils;

@Service
@AllArgsConstructor
public class ZoneTilingTaskService {
  private final ZoneTilingTaskRepository repository;
  private final ZoneTilingJobService zoneTilingJobService;
  private final Mailer mailer;
  public static final String EMAIL_OBJECT = "Resultat du traitement de zone";
  public static final String ZONE_TILING_TEMPLATE_NAME = "zone_tiling";

  public ZoneTilingTask update(ZoneTilingTask zoneTilingTask) {
    if (!repository.existsById(zoneTilingTask.getId())) {
      throw new NotFoundException("ZoneTilingTask.Id = " + zoneTilingTask.getId() + " not found");
    }
    return repository.save(zoneTilingTask);
  }

  public ZoneTilingTask updateStatus(ZoneTilingTask zoneTilingTask, TaskStatus status) {
    zoneTilingTask.addStatus(status);
    return update(zoneTilingTask);
  }

  public void finishWithSuccess(ZoneTilingTask zoneTilingTask) {
    ZoneTilingTask finalized =
        updateStatus(
            zoneTilingTask,
            TaskStatus.builder()
                .id(randomUUID().toString())
                .creationDatetime(now())
                .progression(FINISHED)
                .health(SUCCEEDED)
                .taskId(zoneTilingTask.getId())
                .build());
    ZoneTilingJob associatedJob = zoneTilingJobService.findById(zoneTilingTask.getJobId());
    if (associatedJob.isDone()) {
      Context context = new Context();
      context.setVariable("zone", associatedJob.getZoneName());
      zoneTilingJobService.finishWithSuccess(associatedJob);
      String emailBody = HTMLUtils.parseToString(ZONE_TILING_TEMPLATE_NAME,context);
      try {
        mailer.accept(
            new Email(
                new InternetAddress(associatedJob.getEmailReceiver()),
                List.of(),
                List.of(),
                EMAIL_OBJECT,
                emailBody,
                List.of()));
      } catch (AddressException e) {
        throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, e);
      }
    }
  }
}
