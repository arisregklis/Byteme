package org.byteme.kiosk;

public class KioskController {

    private final int currentFloor;
    private final int totalFloors;
    private String status = "IDLE";
    private String lastMessage = "";

    public KioskController(int currentFloor, int totalFloors) {
        if (totalFloors < 3 || totalFloors > 200) {
            throw new IllegalArgumentException("Invalid total floors");
        }
        if (currentFloor < 0 || currentFloor >= totalFloors) {
            throw new IllegalArgumentException("Invalid current floor");
        }
        this.currentFloor = currentFloor;
        this.totalFloors = totalFloors;
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

    public String buildRequestPayload(int destinationFloor) {
        if (!isValidDestination(destinationFloor)) {
            status = "INVALID_INPUT";
            lastMessage = "400: Invalid destination floor";
            throw new IllegalArgumentException("400: Invalid destination floor");
        }
        status = "SENDING_REQUEST";
        lastMessage = "Prepared request for floor " + destinationFloor;
        return "{\"where_from\":" + currentFloor + ",\"where_to\":" + destinationFloor + "}";
    }

    public String handleServerResponse(int statusCode, String responseBody) {
        if (statusCode == 200) {
            status = "DISPLAYING_ASSIGNMENT";
            lastMessage = "Assigned: " + responseBody;
            return lastMessage;
        }
        if (statusCode == 400) {
            status = "INVALID_INPUT";
            lastMessage = "400 Bad Request: " + responseBody;
            return lastMessage;
        }
        if (statusCode == 503) {
            status = "UNAVAILABLE";
            lastMessage = "503 Service Unavailable: " + responseBody;
            return lastMessage;
        }
        status = "UNAVAILABLE";
        lastMessage = "Unexpected status " + statusCode + ": " + responseBody;
        return lastMessage;
    }

    public String sendRequest(int destinationFloor) {
        String payload = buildRequestPayload(destinationFloor);
        lastMessage = "Sent payload: " + payload;
        return payload;
    }

    public String formatDisplayMessage(String requestId, String elevatorId) {
        if (requestId == null || requestId.isBlank()) {
            status = "INVALID_INPUT";
            lastMessage = "400: Missing request_id";
            throw new IllegalArgumentException("400: Missing request_id");
        }
        if (elevatorId == null || elevatorId.length() != 1
                || elevatorId.charAt(0) < 'A' || elevatorId.charAt(0) > 'T') {
            status = "INVALID_INPUT";
            lastMessage = "400: Invalid elevator_id";
            throw new IllegalArgumentException("400: Invalid elevator_id");
        }
        status = "DISPLAYING_ASSIGNMENT";
        lastMessage = "Request " + requestId + " -> Elevator " + elevatorId;
        return lastMessage;
    }

    public String getStatus() {
        return status;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}