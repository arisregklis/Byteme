package gr.aueb.byteme.evaluation;

public interface EvaluationSystem {

    void recordCompletedRequest(long waitingTimeMs)
            throws InvalidEvaluationDataException;

    void recordFailedRequest();

    int getTotalRequests();

    int getCompletedRequests();

    int getFailedRequests();

    double getAverageWaitingTimeMs();

    void displayResults();
}