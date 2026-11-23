Markdown# Πληκτρολόγιο Χρήστη (Kiosk) — Σχεδιασμός & Λογική

## 1. Σκοπός

Το Πληκτρολόγιο Χρήστη βρίσκεται στον όροφο και λειτουργεί ως σημείο επαφής του χρήστη με το σύστημα.
Παίρνει τον όροφο προορισμού, τον ελέγχει, ετοιμάζει/στέλνει το αίτημα στο Κεντρικό Σύστημα και εμφανίζει ποιο ασανσέρ αντιστοιχίστηκε.

Ο βασικός κανόνας είναι:

> Αν η είσοδος του χρήστη ή το μήνυμα της οθόνης δεν είναι έγκυρο, δεν προχωράει κανονική ενέργεια και επιστρέφεται σφάλμα.

---

## 2. Λειτουργίες που θα υλοποιήσουμε

1. Έλεγχος έγκυρου ορόφου προορισμού.
2. Δημιουργία αιτήματος κλήσης ανελκυστήρα (`where_from`, `where_to`).
3. Έλεγχος και εμφάνιση ενημέρωσης οθόνης (`elevator_id` από `A` έως `T`).
4. Διαχείριση απαντήσεων (`200`, `400`, `503`).
5. Διαχείριση καταστάσεων και σφαλμάτων του πληκτρολογίου.
6. Απλή δομή κλάσης και μεθόδων για το Kiosk.

---

## 3. Καταστάσεις λειτουργίας

```java
public enum KioskStatus {
    IDLE,
    SENDING_REQUEST,
    DISPLAYING_ASSIGNMENT,
    INVALID_INPUT,
    UNAVAILABLE
}
ΠεριγραφήΚατάστασηΣημασίαIDLEΑναμένει επιλογή ορόφου από τον χρήστηSENDING_REQUESTΣτέλνει το αίτημα στο Κεντρικό ΣύστημαDISPLAYING_ASSIGNMENTΕμφανίζει στην οθόνη ποιο ασανσέρ αντιστοιχίστηκεINVALID_INPUTΔόθηκε άκυρος όροφος ή άκυρο μήνυμα οθόνηςUNAVAILABLEΤο σύστημα δεν είναι διαθέσιμο4. Κανόνες ελέγχου ορόφουΟ όροφος προορισμού επιτρέπεται μόνο όταν:PlaintextdestinationFloor >= 0
destinationFloor < totalFloors
destinationFloor != currentFloor
Αν ο όροφος είναι εκτός ορίων ή ίδιος με τον τρέχοντα όροφο:Δεν δημιουργείται έγκυρο αίτημα κίνησης.Επιστρέφεται / καταγράφεται σφάλμα (400 Bad Request).5. Κλάση και μέθοδοι ΠληκτρολογίουJavapublic class KioskController {

    private final int currentFloor;
    private final int totalFloors;
    private KioskStatus status = KioskStatus.IDLE;

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
            status = KioskStatus.INVALID_INPUT;
            throw new IllegalArgumentException("400: Invalid destination floor");
        }
        status = KioskStatus.SENDING_REQUEST;
        return "{\"where_from\":" + currentFloor + ",\"where_to\":" + destinationFloor + "}";
    }

    public String formatDisplayMessage(String requestId, String elevatorId) {
        if (requestId == null || requestId.isBlank()) {
            status = KioskStatus.INVALID_INPUT;
            throw new IllegalArgumentException("400: Missing request_id");
        }
        if (elevatorId == null || elevatorId.length() != 1
                || elevatorId.charAt(0) < 'A' || elevatorId.charAt(0) > 'T') {
            status = KioskStatus.INVALID_INPUT;
            throw new IllegalArgumentException("400: Invalid elevator_id");
        }
        status = KioskStatus.DISPLAYING_ASSIGNMENT;
        return "Request " + requestId + " -> Elevator " + elevatorId;
    }

    public KioskStatus getStatus() {
        return status;
    }
}
6. Μορφή αιτήματος και απαντήσεωνΑίτημα προς το Κεντρικό Σύστημα:JSON{
  "where_from": 2,
  "where_to": 7
}
Απαντήσεις που διαχειρίζεται το Πληκτρολόγιο:200 OK: Το αίτημα έγινε δεκτό και αντιστοιχίστηκε ανελκυστήρας.400 Bad Request: Άκυρος όροφος ή άκυρα δεδομένα.503 Service Unavailable: Το σύστημα δεν είναι προσωρινά διαθέσιμο.
7. Ενημέρωση οθόνης ορόφουΌταν έρχεται αντιστοίχιση ανελκυστήρα:Javaif (elevatorId == null || elevatorId.length() != 1
        || elevatorId.charAt(0) < 'A' || elevatorId.charAt(0) > 'T') {
    status = KioskStatus.INVALID_INPUT;
} else {
    status = KioskStatus.DISPLAYING_ASSIGNMENT;
}
Αν το elevatorId δεν είναι ένα γράμμα από το A έως το T, το μήνυμα απορρίπτεται.
8. Απαραίτητα testsΈγκυρος προορισμός φτιάχνει σωστά το JSON (where_from, where_to).Άκυρος προορισμός (εκτός ορίων ή ίδιος όροφος) ρίχνει σφάλμα και αλλάζει κατάσταση σε INVALID_INPUT.Έγκυρο elevatorId (A–T) επιστρέφει σωστό μήνυμα οθόνης.Άκυρο elevatorId (π.χ. Z ή κενό requestId) απορρίπτεται.
9. Προτεραιότητες υλοποίησηςΠρώτη προτεραιότηταΚαταστάσεις KioskStatus.Έλεγχος έγκυρου ορόφου (isValidDestination).Δημιουργία αιτήματος (buildRequestPayload).Έλεγχος μηνύματος οθόνης (formatDisplayMessage).Βασικά unit tests.Δεύτερη προτεραιότηταΣύνδεση με πραγματικές κλήσεις REST / endpoints.Πλήρης ενσωμάτωση με την οθόνη και το Κεντρικό Σύστημα.