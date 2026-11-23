package gr.aueb.byteme.evaluation;

public interface EvaluationService {

    void recordEvent(
            String elevatorId,
            long timestamp,
            String eventType,
            Integer floor
    );

    void recordMetric(
            String elevatorId,
            String metricName,
            Double value,
            Integer passengersOnboard
    );

    int getTotalEvents();

    int getTotalMetrics();
}