package school.hei.geotiler.repository.model;

import java.util.List;

public abstract class ProgressiveAction<T extends Status> {
  protected static Status.ProgressionStatus checkProgressionTransition(
      Status.ProgressionStatus oldProgression, Status.ProgressionStatus newProgression) {
    RuntimeException illegalProgressionTransition =
        new RuntimeException(
            String.format("illegal transition: %s -> %s", oldProgression, newProgression));
    return switch (oldProgression) {
      case PENDING -> newProgression;
      case PROCESSING -> switch (newProgression) {
        case PENDING -> throw illegalProgressionTransition;
        case PROCESSING, FINISHED -> newProgression;
      };
      case FINISHED -> throw illegalProgressionTransition;
    };
  }

  protected static Status.HealthStatus checkHealthTransition(
      Status.HealthStatus oldHealth, Status.HealthStatus newHealth) {
    RuntimeException illegalHealthTransition =
        new RuntimeException(String.format("illegal transition: %s -> %s", oldHealth, newHealth));
    return switch (oldHealth) {
      case UNKNOWN -> newHealth;
      case SUCCEEDED -> switch (newHealth) {
        case SUCCEEDED -> newHealth;
        case UNKNOWN, FAILED -> throw illegalHealthTransition;
      };
      case FAILED -> switch (newHealth) {
        case FAILED -> newHealth;
        case UNKNOWN, SUCCEEDED -> throw illegalHealthTransition;
      };
    };
  }

  protected void checkStatusTransition(Status oldStatus, Status newStatus) {
    if (newStatus.getCreationDatetime().isBefore(oldStatus.getCreationDatetime())) {
      throw new RuntimeException("newStatus.instant must be after oldStatus.instant");
    }

    checkProgressionTransition(oldStatus.getProgression(), newStatus.getProgression());
    checkHealthTransition(oldStatus.getHealth(), newStatus.getHealth());
  }

  protected abstract List<T> checkStatusHistory(List<T> statusHistory);

  public abstract T getStatus();

  public abstract void addStatus(T status);
}
