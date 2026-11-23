package gr.aueb.byteme.evaluation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gr.aueb.byteme.common.Http;
import gr.aueb.byteme.common.HttpResult;
import gr.aueb.byteme.common.Json;
import gr.aueb.byteme.common.Request;
import java.io.IOException;

// Η διεπαφή REST της αξιολόγησης (API.md §5).
//
//   POST /events             -> καταγράφει ένα γεγονός από ανελκυστήρα
//   POST /runs               -> τρέχει ένα σενάριο: φτιάχνει αιτήματα και τα στέλνει στο κεντρικό
//   GET  /results/{run_id}   -> τα αποτελέσματα ενός σεναρίου
//
// Αρχική έκδοση (API.md §5.4): οι χρόνοι αναμονής/διαδρομής είναι σταθερά 0,
// και "completed" = όσα αιτήματα δέχτηκε το κεντρικό με 202.
public class EvaluationApi implements HttpHandler {
    private String centralUrl;
    private int floors;
    private Scenario scenario = new UniformScenario();   // για νέο σενάριο: νέα κλάση που κάνει implements Scenario
    private Http http = new Http();

    private String[] allowedEvents = {
        "ASSIGNMENT_RECEIVED", "MOVEMENT_STARTED", "FLOOR_REACHED", "DOORS_OPENED",
        "DOORS_CLOSED", "TRIP_COMPLETED", "OVERLOAD_DETECTED", "STOPPED", "RESUMED",
        "FAULT_DETECTED", "EMERGENCY_ACTIVATED"
    };

    // γεγονότα που έχουμε δει (για να μη μετρήσουμε το ίδιο event_id δύο φορές)
    private String[] eventIds = new String[10000];
    private int eventCount = 0;

    // σενάρια που έτρεξαν: η θέση i σε κάθε πίνακα είναι το ίδιο σενάριο
    private String[] runIds = new String[100];
    private String[] runStatus = new String[100];
    private int[] runCompleted = new int[100];
    private int[] runFailed = new int[100];
    private int runCount = 0;

    public EvaluationApi(String centralUrl, int floors) {
        this.centralUrl = centralUrl;
        this.floors = floors;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.equals("/events")) {
                if (isMethod(exchange, "POST")) {
                    newEvent(exchange);
                }
            } else if (path.equals("/runs")) {
                if (isMethod(exchange, "POST")) {
                    newRun(exchange);
                }
            } else if (path.startsWith("/results/")) {
                if (isMethod(exchange, "GET")) {
                    String runId = path.substring("/results/".length());   // "/results/run-1" -> "run-1"
                    results(exchange, runId);
                }
            } else {
                http.sendError(exchange, 404, "NOT_FOUND", "unknown endpoint " + path);
            }
        } catch (IllegalArgumentException e) {
            http.sendError(exchange, 400, "BAD_REQUEST", e.getMessage());
        } catch (RuntimeException e) {
            http.sendError(exchange, 500, "INTERNAL_ERROR", e.getMessage());
        }
    }

    // POST /events  (API.md §5.1)
    // synchronized = ένα event τη φορά, για να μη γράψουν δύο ταυτόχρονα στην ίδια θέση του πίνακα.
    private synchronized void newEvent(HttpExchange exchange) throws IOException {
        Json body = new Json(http.readBody(exchange));
        String eventId = body.getString("event_id");
        String elevatorId = body.getString("elevator_id");
        String eventType = body.getString("event_type");

        if (!isAllowedEvent(eventType)) {
            http.sendError(exchange, 400, "INVALID_EVENT_TYPE", "unknown event_type " + eventType);
            return;
        }

        if (isKnownEvent(eventId)) {
            System.out.println("event " + eventId + " ξαναήρθε, δεν το μετράμε δεύτερη φορά");
        } else {
            eventIds[eventCount] = eventId;
            eventCount++;
            System.out.println("event #" + eventCount + ": ανελκυστήρας " + elevatorId + " -> " + eventType);
        }
        http.sendJson(exchange, 200, "{\"accepted\": true}");
    }

    // POST /runs  (API.md §5.2)   {"scenario": "uniform", "seed": 12345, "passengers": 10}
    private void newRun(HttpExchange exchange) throws IOException {
        Json body = new Json(http.readBody(exchange));
        String scenarioName = body.getString("scenario");
        int seed = body.getInt("seed");
        int passengers = body.getInt("passengers");

        if (scenarioName.equals("morning_rush") || scenarioName.equals("evening_rush")) {
            http.sendError(exchange, 400, "SCENARIO_NOT_READY",
                    scenarioName + " is planned for the next version; use uniform");
            return;
        }
        if (!scenarioName.equals(scenario.getName())) {
            http.sendError(exchange, 400, "INVALID_SCENARIO", "scenario must be uniform");
            return;
        }
        if (passengers < 1 || passengers > 1000) {
            http.sendError(exchange, 400, "INVALID_PASSENGERS", "passengers must be between 1 and 1000");
            return;
        }

        // 1. Νέο run και απάντηση 202
        int index = runCount;
        runCount++;
        String runId = "run-" + runCount;
        runIds[index] = runId;
        runStatus[index] = "RUNNING";
        http.sendJson(exchange, 202, "{\"run_id\": \"" + runId + "\", \"status\": \"RUNNING\"}");

        // 2. Φτιάχνουμε τα αιτήματα και τα στέλνουμε ένα-ένα στο κεντρικό
        Request[] requests = scenario.generate(floors, passengers, seed);
        for (int i = 0; i < requests.length; i++) {
            String json = "{\"request_id\": \"" + runId + "-req-" + (i + 1) + "\", "
                    + "\"source_floor\": " + requests[i].getSourceFloor() + ", "
                    + "\"destination_floor\": " + requests[i].getDestinationFloor() + "}";
            try {
                HttpResult answer = http.post(centralUrl + "/requests", json);
                if (answer.getStatus() == 202) {
                    runCompleted[index]++;
                } else {
                    runFailed[index]++;
                }
            } catch (IllegalStateException e) {
                runFailed[index]++;          // το κεντρικό δεν απαντά
            }
        }
        runStatus[index] = "COMPLETED";
        System.out.println(runId + ": " + runCompleted[index] + " δεκτά, " + runFailed[index] + " απέτυχαν");
    }

    // GET /results/{run_id}  (API.md §5.3)
    private void results(HttpExchange exchange, String runId) throws IOException {
        for (int i = 0; i < runCount; i++) {
            if (runIds[i].equals(runId)) {
                http.sendJson(exchange, 200, "{\"run_id\": \"" + runId + "\", "
                        + "\"status\": \"" + runStatus[i] + "\", "
                        + "\"completed_requests\": " + runCompleted[i] + ", "
                        + "\"failed_requests\": " + runFailed[i] + ", "
                        + "\"average_waiting_time_ms\": 0, "
                        + "\"average_travel_time_ms\": 0}");
                return;
            }
        }
        http.sendError(exchange, 404, "UNKNOWN_RUN", "unknown run_id " + runId);
    }

    private boolean isAllowedEvent(String eventType) {
        for (int i = 0; i < allowedEvents.length; i++) {
            if (allowedEvents[i].equals(eventType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKnownEvent(String eventId) {
        for (int i = 0; i < eventCount; i++) {
            if (eventIds[i].equals(eventId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMethod(HttpExchange exchange, String expected) throws IOException {
        if (exchange.getRequestMethod().equals(expected)) {
            return true;
        }
        http.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "use " + expected);
        return false;
    }
}
