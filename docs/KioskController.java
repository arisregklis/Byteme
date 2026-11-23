package gr.aueb.byteme.kiosk;

// Το πληκτρολόγιο ενός ορόφου. Δεν ξέρει τίποτα για HTTP: αυτό το κάνει το KioskApi.
public class KioskController implements KioskActions {
    private int currentFloor;
    private int totalFloors;
    private String status;

    private String[] sentRequestIds = new String[1000];   // τα αιτήματα που στείλαμε
    private int sentCount = 0;

    public KioskController(int currentFloor, int totalFloors) {
        if (currentFloor < 0 || currentFloor >= totalFloors) {
            throw new IllegalArgumentException("400: kiosk floor must be between 0 and " + (totalFloors - 1));
        }
        this.currentFloor = currentFloor;
        this.totalFloors = totalFloors;
        this.status = "IDLE";
    }

    public boolean isValidDestination(int destinationFloor) {
        if (destinationFloor < 0 || destinationFloor >= totalFloors) {
            return false;
        }
        if (destinationFloor == currentFloor) {
            return false;
        }
        return true;
    }

    public String buildRequestPayload(String requestId, int destinationFloor, String callbackUrl) {
        if (requestId == null || requestId.equals("")) {     // πρώτα το null!
            status = "INVALID_INPUT";
            throw new IllegalArgumentException("400: request_id is required");
        }
        if (!isValidDestination(destinationFloor)) {
            status = "INVALID_INPUT";
            throw new IllegalArgumentException("400: Invalid destination floor");
        }
        sentRequestIds[sentCount] = requestId;
        sentCount++;
        status = "WAITING_FOR_ELEVATOR";

        return "{\"request_id\": \"" + requestId + "\", "
                + "\"source_floor\": " + currentFloor + ", "
                + "\"destination_floor\": " + destinationFloor + ", "
                + "\"callback_url\": \"" + callbackUrl + "\"}";
    }

    public boolean isKnownRequest(String requestId) {
        for (int i = 0; i < sentCount; i++) {
            if (sentRequestIds[i].equals(requestId)) {
                return true;
            }
        }
        return false;
    }

    public String formatDisplayMessage(String requestId, String elevatorId) {
        if (elevatorId == null || elevatorId.length() != 1
                || elevatorId.charAt(0) < 'A' || elevatorId.charAt(0) > 'T') {
            status = "INVALID_INPUT";
            throw new IllegalArgumentException("400: Invalid elevator_id");
        }
        status = "DISPLAYING_ASSIGNMENT";
        return "Χρησιμοποιήστε τον ανελκυστήρα " + elevatorId;   // API.md §4.2
    }

    public String getStatus() {
        return status;
    }
}