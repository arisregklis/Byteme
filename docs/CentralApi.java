Ο ΠΑΡΑΚΑΤΩ ΚΩΔΙΚΑΣ ΕΙΝΑΙ ΠΡΟΪΟΝ AI 
package gr.aueb.byteme.central;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gr.aueb.byteme.common.Http;
import gr.aueb.byteme.common.HttpResult;
import gr.aueb.byteme.common.Json;
import gr.aueb.byteme.common.Request;
import java.io.IOException;

// Η διεπαφή REST του κεντρικού (API.md §2).
//
//   POST /requests   -> νέο αίτημα: διαλέγει ανελκυστήρα, στέλνει στάσεις, ενημερώνει το kiosk
//   GET  /elevators  -> η κατάσταση όλων των ανελκυστήρων
//   GET  /config     -> όροφοι και ανελκυστήρες του κτιρίου
public class CentralApi implements HttpHandler {
    private int floors;
    private String[] elevatorIds;     // {"A", "B", "C"}
    private String[] elevatorUrls;    // {"http://localhost:3001", ...}
    private DispatchAlgorithm algorithm;
    private Http http = new Http();

    private String[] seenRequestIds = new String[10000];   // για το 409 (ίδιο request_id)
    private int seenCount = 0;

    public CentralApi(int floors, int elevators, DispatchAlgorithm algorithm) {
        this.floors = floors;
        this.algorithm = algorithm;
        this.elevatorIds = new String[elevators];
        this.elevatorUrls = new String[elevators];
        String letters = "ABCDEFGHIJKLMNOPQRST";
        for (int i = 0; i < elevators; i++) {
            elevatorIds[i] = letters.substring(i, i + 1);              // A, B, C ...
            elevatorUrls[i] = "http://localhost:" + (3001 + i);        // API.md §0.3
        }
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        System.out.println(exchange.getRequestMethod() + " " + path);

        try {
            if (path.equals("/requests")) {
                if (isMethod(exchange, "POST")) {
                    newRequest(exchange);
                }
            } else if (path.equals("/elevators")) {
                if (isMethod(exchange, "GET")) {
                    allElevators(exchange);
                }
            } else if (path.equals("/config")) {
                if (isMethod(exchange, "GET")) {
                    http.sendJson(exchange, 200, configJson());
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

    // POST /requests  (API.md §2.1)
    private void newRequest(HttpExchange exchange) throws IOException {
        // 1. Διαβάζουμε το αίτημα
        Json body = new Json(http.readBody(exchange));
        String requestId = body.getString("request_id");
        int source = body.getInt("source_floor");
        int destination = body.getInt("destination_floor");
        String callbackUrl = "";
        if (body.has("callback_url")) {
            callbackUrl = body.getString("callback_url");
        }

        // 2. Έλεγχοι -> 400 ή 409
        if (requestId.equals("")) {
            http.sendError(exchange, 400, "MISSING_REQUEST_ID", "request_id is required");
            return;
        }
        if (source < 0 || source >= floors || destination < 0 || destination >= floors) {
            http.sendError(exchange, 400, "INVALID_FLOOR", "Floor must be between 0 and " + (floors - 1));
            return;
        }
        if (source == destination) {
            http.sendError(exchange, 400, "SAME_FLOOR", "destination_floor must differ from source_floor");
            return;
        }
        if (!callbackUrl.equals("") && !callbackUrl.startsWith("http://")) {
            http.sendError(exchange, 400, "INVALID_CALLBACK_URL", "callback_url must start with http://");
            return;
        }
        if (isKnownRequest(requestId)) {
            http.sendError(exchange, 409, "DUPLICATE_REQUEST", "request_id " + requestId + " already exists");
            return;
        }

        // 3. Ρωτάμε όλους τους ανελκυστήρες (GET /state) και διαλέγουμε
        ElevatorState[] states = readAllStates();
        String chosen = algorithm.chooseElevator(states, new Request(source, destination));
        if (chosen == null) {
            http.sendError(exchange, 503, "NO_ELEVATOR_AVAILABLE", "No elevator is available");
            return;
        }

        // 4. Απαντάμε 202 στον καλούντα
        seenRequestIds[seenCount] = requestId;
        seenCount++;
        http.sendJson(exchange, 202, "{\"request_id\": \"" + requestId + "\", \"status\": \"PENDING\"}");
        System.out.println("  " + requestId + ": " + source + " -> " + destination + "  =>  ανελκυστήρας " + chosen);

        // 5. Στέλνουμε τις δύο στάσεις στον ανελκυστήρα (POST /stops)
        String elevatorUrl = elevatorUrls[indexOf(chosen)];
        sendStop(elevatorUrl, requestId, source, "PICKUP");
        sendStop(elevatorUrl, requestId, destination, "DROPOFF");

        // 6. Ενημερώνουμε το kiosk (POST /display), αν μας έδωσε callback_url
        if (!callbackUrl.equals("")) {
            try {
                http.post(callbackUrl,
                        "{\"request_id\": \"" + requestId + "\", \"elevator_id\": \"" + chosen + "\"}");
            } catch (IllegalStateException e) {
                System.out.println("  το kiosk δεν απαντά στο " + callbackUrl);
            }
        }
    }

    // GET /elevators  (API.md §2.2)
    private void allElevators(HttpExchange exchange) throws IOException {
        String json = "{\"elevators\": [";
        for (int i = 0; i < elevatorIds.length; i++) {
            if (i > 0) {
                json = json + ", ";
            }
            try {
                HttpResult answer = http.get(elevatorUrls[i] + "/state");
                json = json + answer.getBody();          // το JSON του GET /state αυτούσιο
            } catch (IllegalStateException e) {
                json = json + "{\"elevator_id\": \"" + elevatorIds[i] + "\", \"error\": \"unreachable\"}";
            }
        }
        json = json + "]}";
        http.sendJson(exchange, 200, json);
    }

    // GET /config  (API.md §2.3)
    private String configJson() {
        String ids = "[";
        for (int i = 0; i < elevatorIds.length; i++) {
            if (i > 0) {
                ids = ids + ", ";
            }
            ids = ids + "\"" + elevatorIds[i] + "\"";
        }
        ids = ids + "]";
        return "{\"floors\": " + floors + ", \"elevators\": " + elevatorIds.length
                + ", \"elevator_ids\": " + ids + "}";
    }

    // Ρωτάει κάθε ανελκυστήρα GET /state.
    // Όποιος δεν απαντά, μετράει σαν OUT_OF_SERVICE, οπότε ο αλγόριθμος τον προσπερνά.
    private ElevatorState[] readAllStates() {
        ElevatorState[] states = new ElevatorState[elevatorIds.length];
        for (int i = 0; i < elevatorIds.length; i++) {
            try {
                HttpResult answer = http.get(elevatorUrls[i] + "/state");
                Json state = new Json(answer.getBody());
                states[i] = new ElevatorState(elevatorIds[i],
                        state.getInt("floor"),
                        state.getString("status"),
                        state.countItems("next_stops"));
            } catch (RuntimeException e) {
                states[i] = new ElevatorState(elevatorIds[i], 0, "OUT_OF_SERVICE", 0);
            }
        }
        return states;
    }

    private void sendStop(String elevatorUrl, String requestId, int floor, String type) {
        String json = "{\"request_id\": \"" + requestId + "\", \"floor\": " + floor
                + ", \"type\": \"" + type + "\"}";
        try {
            HttpResult answer = http.post(elevatorUrl + "/stops", json);
            if (answer.getStatus() != 200) {
                System.out.println("  ο ανελκυστήρας απάντησε " + answer.getStatus() + ": " + answer.getBody());
            }
        } catch (IllegalStateException e) {
            System.out.println("  ο ανελκυστήρας δεν απαντά: " + elevatorUrl);
        }
    }

    private int indexOf(String elevatorId) {
        for (int i = 0; i < elevatorIds.length; i++) {
            if (elevatorIds[i].equals(elevatorId)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isKnownRequest(String requestId) {
        for (int i = 0; i < seenCount; i++) {
            if (seenRequestIds[i].equals(requestId)) {
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
