package gr.aueb.byteme.evaluation;

public class SimpleEvaluationSystem implements EvaluationSystem {

    private int completedRequests;
    private int failedRequests;
    private long totalWaitingTimeMs;

    public SimpleEvaluationSystem() {
        completedRequests = 0;
        failedRequests = 0;
        totalWaitingTimeMs = 0;
    }

    @Override
    public void recordCompletedRequest(long waitingTimeMs)
            throws InvalidEvaluationDataException {

        if (waitingTimeMs < 0) {
            throw new InvalidEvaluationDataException(
                    "Ο χρόνος αναμονής δεν μπορεί να είναι αρνητικός."
            );
        }

        completedRequests++;
        totalWaitingTimeMs += waitingTimeMs;
    }

    @Override
    public void recordFailedRequest() {
        failedRequests++;
    }

    @Override
    public int getTotalRequests() {
        return completedRequests + failedRequests;
    }

    @Override
    public int getCompletedRequests() {
        return completedRequests;
    }

    @Override
    public int getFailedRequests() {
        return failedRequests;
    }

    @Override
    public double getAverageWaitingTimeMs() {
        if (completedRequests == 0) {
            return 0.0;
        }

        return (double) totalWaitingTimeMs / completedRequests;
    }

    @Override
    public void displayResults() {
        System.out.println("----- ΑΠΟΤΕΛΕΣΜΑΤΑ ΑΞΙΟΛΟΓΗΣΗΣ -----");
        System.out.println("Συνολικά αιτήματα: " + getTotalRequests());
        System.out.println(
                "Επιτυχημένα αιτήματα: " + getCompletedRequests()
        );
        System.out.println(
                "Αποτυχημένα αιτήματα: " + getFailedRequests()
        );
        System.out.println(
                "Μέσος χρόνος αναμονής: "
                + getAverageWaitingTimeMs()
                + " ms"
        );
    }
}