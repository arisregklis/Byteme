package gr.aueb.byteme.central;

import com.sun.net.httpserver.HttpServer;
import gr.aueb.byteme.common.Args;
import java.io.IOException;
import java.net.InetSocketAddress;

public class CentralMain {

    public static void main(String[] args) {
        Args params = new Args(args);
        int port = params.getInt("--port", 3000);
        int floors = params.getInt("--floors", 10);
        int elevators = params.getInt("--elevators", 3);

        if (floors < 3 || floors > 200) {
            System.err.println("Οι όροφοι πρέπει να είναι μεταξύ 3 και 200");
            return;
        }
        if (elevators < 2 || elevators > 20) {
            System.err.println("Οι ανελκυστήρες πρέπει να είναι από 2 εώς 20")
            return;
        }

        DispatchAlgorithm algorithm = new SimpleDispatcher();

        try {
            HttpSercer server = HttpServer.create(newInetSocketAddress(port), 0);
            sever.createContext("/", new CentralApi(floors, elevators, algorithm));
            server.start();
        } catch (IOException e) {
            System.err.println("Δεν ξεκίνησε ο server στη θύρα " + port + ": " + e.getMessage());
            System.err.println("Δοκίμασε άλλη θύρα");
            return;
        }
        int lastElevatorPort = port + elevators;
        System.out.println("Κεντρικό σύστημα στο http://localhost:" + port
                + " | " + floors + " όροφοι, " + elevators + " ανελκυστήρες"
                + " (θύρες " + (port + 1) + "-" + lastElevatorPort + ")");
    }
}