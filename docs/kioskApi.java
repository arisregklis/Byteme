package gr.aueb.byteme.kiosk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gr.aueb.byteme.common.Http;
import gr.aueb.byteme.common.Json;
import java.io.IOException;

// Η διεπαφή REST του kiosk (API.md §4.2). Έχει ένα μόνο endpoint:
//
//   POST /display  {"request_id": "req-2-1", "elevator_id": "C"}
//
// Το καλεί το κεντρικό όταν διαλέξει ανελκυστήρα.
public class KioskApi implements HttpHandler {
    private KioskActions kiosk;
    private Http http = new Http();

    public KioskApi(KioskActions kiosk) {
        this.kiosk = kiosk;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        try {
            if (!path.equals("/display")) {
                http.sendError(exchange, 404, "NOT_FOUND", "unknown endpoint " + path);
                return;
            }
            if (!exchange.getRequestMethod().equals("POST")) {
                http.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "use POST");
                return;
            }

            Json body = new Json(http.readBody(exchange));
            String requestId = body.getString("request_id");
            String elevatorId = body.getString("elevator_id");

            if (!kiosk.isKnownRequest(requestId)) {
                http.sendError(exchange, 404, "UNKNOWN_REQUEST", "unknown request_id " + requestId);
                return;
            }

            String message = kiosk.formatDisplayMessage(requestId, elevatorId);   // άκυρο -> 400
            System.out.println();
            System.out.println("   >>> " + message + " <<<   (" + requestId + ")");
            System.out.print("Πληκτρολόγησε προορισμό (ή q για έξοδο): ");

            http.sendJson(exchange, 200, "{\"shown\": true}");
        } catch (IllegalArgumentException e) {
            http.sendError(exchange, 400, "BAD_REQUEST", e.getMessage());
        }
    }
}