package gr.aueb.byteme.kiosk;

import com.sun.net.httpserver.HttpServer;
import gr.aueb.byteme.common.Args;
import gr.aueb.byteme.common.Http;
import gr.aueb.byteme.common.HttpResult;
import gr.aueb.byteme.common.Json;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

// Εκκίνηση:  --floor 2 --port 3100 --central-url http://localhost:3000
// Προαιρετικά: --floors 10
//
// Το kiosk κάνει δύο δουλειές ταυτόχρονα:
//   1. ακούει στο POST /display (server, για να του πει το κεντρικό το γράμμα)
//   2. διαβάζει από το πληκτρολόγιο και στέλνει POST /requests (client)
public class KioskMain {
    public static void main(String[] args) {
        Args params = new Args(args);
        int floor = params.getInt("--floor", -1);
        int port = params.getInt("--port", 3100);
        String centralUrl = params.getString("--central-url", "http://localhost:3000");
        int floors = params.getInt("--floors", 10);

        if (floor == -1) {
            System.out.println("Λείπει το --floor. Παράδειγμα: --floor 2 --port 3100");
            return;
        }

        KioskActions kiosk;
        try {
            kiosk = new KioskController(floor, floors);
        } catch (IllegalArgumentException e) {
            System.out.println("Λάθος --floor: " + e.getMessage());
            return;
        }

        // 1. Server για το POST /display
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new KioskApi(kiosk));
            server.start();
        } catch (IOException e) {
            System.out.println("Η θύρα " + port + " χρησιμοποιείται ήδη. Δοκίμασε άλλη με --port.");
            return;
        }
        System.out.println("Kiosk ορόφου " + floor + " ακούει στο http://localhost:" + port + "/display");

        // 2. Πληκτρολόγιο
        Http http = new Http();
        Scanner in = new Scanner(System.in);
        int counter = 0;

        while (true) {
            System.out.print("Πληκτρολόγησε προορισμό (ή q για έξοδο): ");
            if (!in.hasNextLine()) {
                break;                       // έκλεισε η είσοδος
            }
            String line = in.nextLine().trim();
            if (line.equals("q")) {
                break;
            }

            int destination;
            try {
                destination = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Γράψε αριθμό ορόφου.");
                continue;
            }
            if (!kiosk.isValidDestination(destination)) {
                System.out.println("Άκυρος όροφος. Δώσε 0 έως " + (floors - 1) + ", όχι τον " + floor + ".");
                continue;
            }

            counter++;
            String requestId = "req-" + floor + "-" + counter;   // μοναδικό: όροφος + αύξων
            String callbackUrl = "http://localhost:" + port + "/display";
            String json = kiosk.buildRequestPayload(requestId, destination, callbackUrl);

            System.out.println("Αίτημα " + requestId + " στάλθηκε, περιμένετε...");
            try {
                HttpResult answer = http.post(centralUrl + "/requests", json);
                if (answer.getStatus() != 202) {
                    // από το JSON σφάλματος κρατάμε μόνο το "message"
                    String message = answer.getBody();
                    Json error = new Json(answer.getBody());
                    if (error.has("message")) {
                        message = error.getString("message");
                    }
                    System.out.println("Το κεντρικό απάντησε " + answer.getStatus() + ": " + message);
                }
            } catch (IllegalStateException e) {
                System.out.println("Το κεντρικό σύστημα δεν απαντά. Δοκιμάστε ξανά.");   // API.md §4.1
            }
        }

        System.out.println("Έξοδος.");
        System.exit(0);                      // σταματά και τον server
    }
}