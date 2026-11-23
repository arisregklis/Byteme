# Πληκτρολόγιο Χρήστη (Kiosk) — Σχεδιασμός & Λογική

## 1. Σκοπός

Το Πληκτρολόγιο Χρήστη βρίσκεται στον όροφο και λειτουργεί ως σημείο επαφής του χρήστη με το σύστημα.
Παίρνει τον όροφο προορισμού, τον ελέγχει, ετοιμάζει/στέλνει το αίτημα στο Κεντρικό Σύστημα και εμφανίζει ποιο ασανσέρ αντιστοιχίστηκε ή ποιο σφάλμα προέκυψε.

Ο βασικός κανόνας είναι:

> Αν η είσοδος του χρήστη ή το μήνυμα της οθόνης δεν είναι έγκυρο, δεν προχωράει κανονική ενέργεια και επιστρέφεται σφάλμα.

---

## 2. Λειτουργίες που θα υλοποιήσουμε

1. Έλεγχος έγκυρου ορόφου προορισμού (`isValidDestination`).
2. Δημιουργία αιτήματος κλήσης ανελκυστήρα (`buildRequestPayload`).
3. Αποστολή αιτήματος στο Κεντρικό Σύστημα (`sendRequest`).
4. Διαχείριση απαντήσεων (`handleServerResponse` για `200`, `400`, `503`).
5. Έλεγχος και εμφάνιση ενημέρωσης οθόνης (`formatDisplayMessage` με `elevator_id` από `A` έως `T`).
6. Καταγραφή τρέχουσας κατάστασης και τελευταίου μηνύματος (`getStatus`, `getLastMessage`).

---

## 3. Καταστάσεις λειτουργίας

Στη βασική έκδοση κρατάμε την κατάσταση ως απλό κείμενο (`String`):

- `"IDLE"`: Αναμένει επιλογή ορόφου από τον χρήστη.
- `"SENDING_REQUEST"`: Στέλνει το αίτημα στο Κεντρικό Σύστημα.
- `"DISPLAYING_ASSIGNMENT"`: Εμφανίζει στην οθόνη ποιο ασανσέρ αντιστοιχίστηκε.
- `"INVALID_INPUT"`: Δόθηκε άκυρος όροφος ή άκυρο μήνυμα οθόνης.
- `"UNAVAILABLE"`: Το σύστημα δεν είναι διαθέσιμο (`503` ή σφάλμα σύνδεσης).

---

## 4. Κανόνες ελέγχου ορόφου

Ο όροφος προορισμού επιτρέπεται μόνο όταν:

```text
destinationFloor >= 0
destinationFloor < totalFloors
destinationFloor != currentFloor
Αν ο όροφος είναι εκτός ορίων ή ίδιος με τον τρέχοντα όροφο:

Δεν δημιουργείται έγκυρο αίτημα κίνησης.

Η κατάσταση γίνεται "INVALID_INPUT".

Επιστρέφεται / καταγράφεται σφάλμα (400 Bad Request).

5. Κλάση και όλες οι μέθοδοι Πληκτρολογίου
Java
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
        // Εδώ θα συνδεθεί η πραγματική κλήση HTTP POST /requests προς το Κεντρικό Σύστημα
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
6. Μορφή αιτήματος και απαντήσεων
Αίτημα προς το Κεντρικό Σύστημα:

JSON
{
  "where_from": 2,
  "where_to": 7
}
Απαντήσεις που διαχειρίζεται το handleServerResponse:

200 OK: Το αίτημα έγινε δεκτό και η κατάσταση γίνεται "DISPLAYING_ASSIGNMENT".

400 Bad Request: Άκυρος όροφος ή άκυρα δεδομένα και η κατάσταση γίνεται "INVALID_INPUT".

503 Service Unavailable: Το σύστημα δεν είναι διαθέσιμο και η κατάσταση γίνεται "UNAVAILABLE".

7. Ενημέρωση οθόνης ορόφου
Όταν έρχεται αντιστοίχιση ανελκυστήρα στο formatDisplayMessage:

Java
if (elevatorId == null || elevatorId.length() != 1
        || elevatorId.charAt(0) < 'A' || elevatorId.charAt(0) > 'T') {
    status = "INVALID_INPUT";
} else {
    status = "DISPLAYING_ASSIGNMENT";
}
Αν το elevatorId δεν είναι ένα γράμμα από το A έως το T, το μήνυμα απορρίπτεται.

8. Απαραίτητα tests
Έγκυρος προορισμός (isValidDestination, buildRequestPayload, sendRequest) φτιάχνει σωστά το JSON (where_from, where_to).

Άκυρος προορισμός (εκτός ορίων ή ίδιος όροφος) ρίχνει σφάλμα και αλλάζει κατάσταση σε "INVALID_INPUT".

Διαχείριση απαντήσεων 200, 400, 503 από το handleServerResponse ενημερώνει σωστά την κατάσταση ("DISPLAYING_ASSIGNMENT", "INVALID_INPUT", "UNAVAILABLE").

Έγκυρο elevatorId (A–T) στο formatDisplayMessage επιστρέφει σωστό μήνυμα οθόνης, ενώ άκυρο (π.χ. Z ή κενό requestId) απορρίπτεται.

9. Προτεραιότητες υλοποίησης
Πρώτη προτεραιότητα
Καταστάσεις με String (status, lastMessage).

Έλεγχος έγκυρου ορόφου (isValidDestination).

Δημιουργία αιτήματος (buildRequestPayload).

Διαχείριση απαντήσεων (handleServerResponse).

Έλεγχος μηνύματος οθόνης (formatDisplayMessage).

Βασικά unit tests.

Δεύτερη προτεραιότητα
Πλήρης αποστολή HTTP στο sendRequest προς τα endpoints του Κεντρικού Συστήματος.

Πλήρης ενσωμάτωση με την οθόνη και τις υπόλοιπες ομάδες