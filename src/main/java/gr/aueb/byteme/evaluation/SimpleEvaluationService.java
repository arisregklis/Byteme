package gr.aueb.byteme.evaluation;

public class SimpleEvaluationService implements EvaluationService {

    private static final int MAX_CAPACITY = 8;

    private int totalEvents;
    private int totalMetrics;

    public SimpleEvaluationService() {
        totalEvents = 0;
        totalMetrics = 0;
    }

    @Override
    public void recordEvent(
            String elevatorId,
            long timestamp,
            String eventType,
            Integer floor
    ) {
        if (elevatorId == null || elevatorId.isBlank()) {
            throw new IllegalArgumentException("elevatorId is required");
        }

        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp cannot be negative");
        }

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }

        totalEvents++;

        System.out.println(
                "Event received: " + eventType
                        + " from elevator " + elevatorId
        );
    }

    @Override
    public void recordMetric(
            String elevatorId,
            String metricName,
            Double value,
            Integer passengersOnboard
    ) {
        if (passengersOnboard != null) {
            if (passengersOnboard < 0) {
                throw new IllegalArgumentException(
                        "passengersOnboard cannot be negative"
                );
            }

            if (passengersOnboard > MAX_CAPACITY) {
                throw new CapacityExceededException(
                        "Overload detected! Max limit is 8 passengers."
                );
            }
        } else {
            if (metricName == null || metricName.isBlank()) {
                throw new IllegalArgumentException("metricName is required");
            }

            if (value == null) {
                throw new IllegalArgumentException("value is required");
            }
        }

        totalMetrics++;

        System.out.println("Metric received from elevator " + elevatorId);
    }

    @Override
    public int getTotalEvents() {
        return totalEvents;
    }

    @Override
    public int getTotalMetrics() {
        return totalMetrics;
    }
}
