package gr.aueb.byteme.evaluation;

public class EvaluationMain {

    public static void main(String[] args) {

        EvaluationSystem evaluation =
                new SimpleEvaluationSystem();

        try {
            evaluation.recordCompletedRequest(8000);
            evaluation.recordCompletedRequest(10000);
            evaluation.recordCompletedRequest(12000);

            evaluation.recordFailedRequest();

            evaluation.displayResults();

        } catch (InvalidEvaluationDataException exception) {
            System.out.println(
                    "Σφάλμα αξιολόγησης: "
                    + exception.getMessage()
            );
        }
    }
}
