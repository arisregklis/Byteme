package gr.aueb.byteme.evaluation;

import com.sun.net.httpserver.HttpServer;
import gr.aueb.byteme.common.Args;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

// Εκκίνηση:  --port 8300 --central-url http://localhost:3000
// Προαιρετικά: --floors 10
public class EvaluationMain {
    public static void main(String[] args) {
        Args params = new Args(args);
        int port = params.getInt("--port", 8300);
        String centralUrl = params.getString("--central-url", "http://localhost:3000");
        int floors = params.getInt("--floors", 10);

        if (floors < 2) {
            System.out.println("Ο αριθμός ορόφων πρέπει να είναι τουλάχιστον 2.");
            return;
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new EvaluationApi(centralUrl, floors));
            // Η αξιολόγηση πρέπει να εξυπηρετεί 4 αιτήματα ταυτόχρονα:
            // όσο τρέχει ένα POST /runs, οι ανελκυστήρες της στέλνουν POST /events.
            // Με έναν μόνο εξυπηρετητή θα περίμεναν όλοι όλους για πάντα (deadlock).
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
        } catch (IOException e) {
            System.out.println("Η θύρα " + port + " χρησιμοποιείται ήδη. Δοκίμασε άλλη με --port.");
            return;
        }

        System.out.println("Αξιολόγηση ακούει στο http://localhost:" + port + " (κεντρικό: " + centralUrl + ")");
    }
}
