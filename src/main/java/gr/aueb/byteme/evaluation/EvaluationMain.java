package gr.aueb.byteme.evaluation;

public class EvaluationMain {

    private static final int PORT = 7003;

    public static void main(String[] args) {

        try {
            EvaluationService evaluationService =
                    new SimpleEvaluationService();

            EvaluationHttpServer server =
                    new EvaluationHttpServer(PORT, evaluationService);

            server.start();

            System.out.println(
                    "Evaluation REST server started at "
                            + "http://localhost:" + PORT
            );

            System.out.println(
                    "POST /api/evaluation/events"
            );

            System.out.println(
                    "POST /api/evaluation/metrics"
            );

        } catch (Exception exception) {
            System.err.println(
                    "Could not start Evaluation server: "
                            + exception.getMessage()
            );
        }
    }
}