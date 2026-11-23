
# Πληκτρολόγιο Χρήστη (Kiosk) — Σχεδιασμός & Λογική

## 1. Σκοπός

Το Πληκτρολόγιο Χρήστη βρίσκεται στον όροφο και λειτουργεί ως σημείο επαφής του χρήστη με το σύστημα.
Παίρνει τον όροφο προορισμού, τον ελέγχει, ετοιμάζει το αίτημα για το Κεντρικό Σύστημα και εμφανίζει ποιο ασανσέρ αντιστοιχίστηκε.

Ο βασικός κανόνας είναι:

> Αν η είσοδος του χρήστη ή το μήνυμα της οθόνης δεν είναι έγκυρο, δεν προχωράει κανονική ενέργεια και επιστρέφεται σφάλμα.

---

## 2. Λειτουργίες που θα υλοποιήσουμε

1. Έλεγχος έγκυρου ορόφου προορισμού.
2. Δημιουργία αιτήματος κλήσης ανελκυστήρα (`where_from`, `where_to`).
3. Έλεγχος και εμφάνιση ενημέρωσης οθόνης (`elevator_id` από `A` έως `T`).
4. Διαχείριση απαντήσεων (`200`, `400`, `503`).
5. Καταγραφή κατάστασης / τελευταίου μηνύματος του πληκτρολογίου.
6. Απλή κλάση και μέθοδοι για το Kiosk.

---

## 3. Καταστάσεις λειτουργίας

Στη βασική έκδοση κρατάμε την κατάσταση ως απλό κείμενο (`String`):

- `"IDLE"`: Αναμένει επιλογή ορόφου από τον χρήστη.
- `"SENDING_REQUEST"`: Στέλνει το αίτημα στο Κεντρικό Σύστημα.
- `"DISPLAYING_ASSIGNMENT"`: Εμφανίζει στην οθόνη ποιο ασανσέρ αντιστοιχίστηκε.
- `"INVALID_INPUT"`: Δόθηκε άκυρος όροφος ή άκυρο μήνυμα οθόνης.
- `"UNAVAILABLE"`: Το σύστημα δεν είναι διαθέσιμο.

---

## 4. Κανόνες ελέγχου ορόφου

Ο όροφος προορισμού επιτρέπεται μόνο όταν:

```text
destinationFloor >= 0
destinationFloor < totalFloors
destinationFloor != currentFloor
Αν ο όροφος είναι εκτός ορίων ή ίδιος με τον τρέχοντα όροφο:

Δεν δημιουργείται έγκυρο αίτημα κίνησης.

Επιστρέφεται / καταγράφεται σφάλμα (400 Bad Request).

5. Κλάση και μέθοδοι Πληκτρολογίου
Java
public class KioskController {

    private final int currentFloor;
    private final int totalFloors;
    private String status = "IDLE";

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
            throw new IllegalArgumentException("400: Invalid destination floor");
        }
        status = "SENDING_REQUEST";
        return "{\"where_from\":" + currentFloor + ",\"where_to\":" + destinationFloor + "}";
    }

    public String formatDisplayMessage(String requestId, String elevatorId) {
        if (requestId == null || requestId.isBlank()) {
            status = "INVALID_INPUT";
            throw new IllegalArgumentException("400: Missing request_id");
        }
        if (elevatorId == null || elevatorId.length() != 1
                || elevatorId.charAt(0) < 'A' || elevatorId.charAt(0) > 'T') {
            status = "INVALID_INPUT";
            throw new IllegalArgumentException("400: Invalid elevator_id");
        }
        status = "DISPLAYING_ASSIGNMENT";
        return "Request " + requestId + " -> Elevator " + elevatorId;
    }

    public String getStatus() {
        return status;
    }
}
6. Μορφή αιτήματος και απαντήσεων
Αίτημα προς το Κεντρικό Σύστημα:

JSON
{
  "where_from": 2,
  "where_to": 7
}
Απαντήσεις που διαχειρίζεται το Πληκτρολόγιο:

200 OK: Το αίτημα έγινε δεκτό και αντιστοιχίστηκε ανελκυστήρας.

400 Bad Request: Άκυρος όροφος ή άκυρα δεδομένα.

503 Service Unavailable: Το σύστημα δεν είναι προσωρινά διαθέσιμο.

7. Ενημέρωση οθόνης ορόφου
Όταν έρχεται αντιστοίχιση ανελκυστήρα:

Java
if (elevatorId == null || elevatorId.length() != 1
        || elevatorId.charAt(0) < 'A' || elevatorId.charAt(0) > 'T') {
    status = "INVALID_INPUT";
} else {
    status = "DISPLAYING_ASSIGNMENT";
}
Αν το elevatorId δεν είναι ένα γράμμα από το A έως το T, το μήνυμα απορρίπτεται.

8. Απαραίτητα tests
Έγκυρος προορισμός φτιάχνει σωστά το JSON (where_from, where_to).

Άκυρος προορισμός (εκτός ορίων ή ίδιος όροφος) ρίχνει σφάλμα και αλλάζει κατάσταση σε "INVALID_INPUT".

Έγκυρο elevatorId (A–T) επιστρέφει σωστό μήνυμα οθόνης.

Άκυρο elevatorId (π.χ. Z ή κενό requestId) απορρίπτεται.

9. Προτεραιότητες υλοποίησης
Πρώτη προτεραιότητα
Κατάσταση με String (status).

Έλεγχος έγκυρου ορόφου (isValidDestination).

Δημιουργία αιτήματος (buildRequestPayload).

Έλεγχος μηνύματος οθόνης (formatDisplayMessage).

Βασικά unit tests.

Δεύτερη προτεραιότητα
Σύνδεση με πραγματικές κλήσεις REST / endpoints.

Πλήρης ενσωμάτωση με την οθόνη και το Κεντρικό Σύστημα.