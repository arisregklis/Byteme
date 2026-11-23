package gr.aueb.byteme.kiosk;

// Η διεπαφή Java του πληκτρολογίου (docs/kiosk.md).
public interface KioskActions {
    // Είναι έγκυρος ο προορισμός; (μέσα στο κτίριο και όχι ο ίδιος όροφος)
    boolean isValidDestination(int destinationFloor);

    // Το JSON για το POST /requests του κεντρικού (API.md §4.1). Άκυρο -> 400.
    String buildRequestPayload(String requestId, int destinationFloor, String callbackUrl);

    // Έχουμε στείλει εμείς αυτό το request_id; (αλλιώς το /display απαντά 404)
    boolean isKnownRequest(String requestId);

    // Το μήνυμα της οθόνης για το POST /display (API.md §4.2). Άκυρο γράμμα -> 400.
    String formatDisplayMessage(String requestId, String elevatorId);

    String getStatus();
}