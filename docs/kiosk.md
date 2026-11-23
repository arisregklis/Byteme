
# Πληκτρολόγιο Χρήστη (Kiosk) — Σχεδιασμός & Λογική

## 1. Σκοπός

Το Πληκτρολόγιο Χρήστη βρίσκεται στον όροφο και λειτουργεί ως σημείο επαφής του χρήστη με το σύστημα.
Παίρνει τον όροφο προορισμού από το πληκτρολόγιο, τον ελέγχει, φτιάχνει το αίτημα για το Κεντρικό Σύστημα (`POST /requests`) και δέχεται την ανάθεση ανελκυστήρα από το Κεντρικό Σύστημα (`POST /display`).

Ο βασικός κανόνας είναι:

> Αν η είσοδος του χρήστη, το αίτημα ή το μήνυμα της οθόνης δεν είναι έγκυρο (ή αφορά άγνωστο `request_id`), δεν προχωράει κανονική ενέργεια και επιστρέφεται το αντίστοιχο σφάλμα.

---

## 2. Αρχιτεκτονική & Αρχεία (`gr.aueb.byteme.kiosk`)

Η υλοποίηση του kiosk χωρίζεται σε τέσσερα αρχεία στο πακέτο `gr.aueb.byteme.kiosk`:

1. **`KioskActions.java`**: Η διεπαφή (`interface`) με τις βασικές πράξεις του kiosk (`isValidDestination`, `buildRequestPayload`, `isKnownRequest`, `formatDisplayMessage`, `getStatus`).
2. **`KioskController.java`**: Υλοποιεί το `KioskActions` με απλή λογική, πίνακα καταγεγραμμένων αιτημάτων (`sentRequestIds` / `sentCount`) και κατάσταση ως `String`. Δεν περιέχει κώδικα HTTP.
3. **`KioskApi.java`**: Υλοποιεί `HttpHandler` και εκθέτει το endpoint `POST /display` για να ενημερώνει το kiosk ποιος ανελκυστήρας αντιστοιχίστηκε.
4. **`KioskMain.java`**: Το σημείο εκκίνησης (`main`) που σηκώνει τον HTTP server για το `/display`, διαβάζει εισόδους από το πληκτρολόγιο και στέλνει `POST /requests` στο Κεντρικό Σύστημα.

---

## 3. Καταστάσεις λειτουργίας (`String`)

Στον `KioskController` η κατάσταση κρατιέται ως απλό `String`:

- `"IDLE"`: Αρχική κατάσταση αναμονής.
- `"WAITING_FOR_ELEVATOR"`: Έχει δημιουργηθεί έγκυρο payload αιτήματος και αναμένεται ανάθεση ανελκυστήρα.
- `"DISPLAYING_ASSIGNMENT"`: Εμφανίστηκε / μορφοποιήθηκε έγκυρο μήνυμα ανάθεσης ανελκυστήρα.
- `"INVALID_INPUT"`: Δόθηκε άκυρο `request_id`, άκυρος όροφος προορισμού ή άκυρο `elevator_id`.

---

## 4. Κανόνες ελέγχου ορόφου & αιτήματος

Ο όροφος προορισμού (`destinationFloor`) είναι έγκυρος όταν:

```text
destinationFloor >= 0
destinationFloor < totalFloors
destinationFloor != currentFloor
Στο buildRequestPayload(requestId, destinationFloor, callbackUrl):

Αν το requestId είναι null ή κενό (""), η κατάσταση γίνεται "INVALID_INPUT" και πετάει IllegalArgumentException.

Αν ο destinationFloor δεν είναι έγκυρος, η κατάσταση γίνεται "INVALID_INPUT" και πετάει IllegalArgumentException.

Αν είναι έγκυρος, αποθηκεύει το requestId στα γνωστά αιτήματα, θέτει κατάσταση "WAITING_FOR_ELEVATOR" και επιστρέφει JSON.

5. Μορφή JSON αιτήματος και endpoints
Αίτημα προς το Κεντρικό Σύστημα (POST /requests)
JSON
{
  "request_id": "req-2-1",
  "source_floor": 2,
  "destination_floor": 7,
  "callback_url": "http://localhost:3100/display"
}
Στο KioskMain, το κεντρικό σύστημα αναμένεται να απαντήσει 202 σε επιτυχημένη αποστολή, ενώ σε σφάλμα διαβάζεται το πεδίο "message" (αν υπάρχει) ή εμφανίζεται μήνυμα μη διαθεσιμότητας αν δεν απαντά.

Ενημέρωση οθόνης (POST /display)
Το Κεντρικό Σύστημα καλεί POST /display με σώμα:

JSON
{
  "request_id": "req-2-1",
  "elevator_id": "C"
}
Στο KioskApi:

Λάθος path -> 404 (NOT_FOUND).

Λάθος μέθοδος (όχι POST) -> 405 (METHOD_NOT_ALLOWED).

Άγνωστο request_id (!kiosk.isKnownRequest(requestId)) -> 404 (UNKNOWN_REQUEST).

Άκυρο elevator_id (όχι ένα γράμμα από 'A' έως 'T') -> IllegalArgumentException -> 400 (BAD_REQUEST) και κατάσταση "INVALID_INPUT".

Έγκυρο elevator_id -> επιστρέφει το μήνυμα "Χρησιμοποιήστε τον ανελκυστήρα " + elevatorId, θέτει κατάσταση "DISPLAYING_ASSIGNMENT" και απαντά 200 με {"shown": true}.

6. Εκκίνηση (KioskMain)
Παράδειγμα εκκίνησης:

Plaintext
--floor 2 --port 3100 --central-url http://localhost:3000 --floors 10
Το --floor είναι υποχρεωτικό (0 έως totalFloors - 1).

Το --port έχει προεπιλογή 3100.

Το --central-url έχει προεπιλογή http://localhost:3000.

Το --floors έχει προεπιλογή 10.

7. Βασικά Tests (KioskControllerTest)
Έγκυρος προορισμός (isValidDestination) επιστρέφει true μόνο για ορόφους εντός [0, totalFloors - 1) διαφορετικούς από currentFloor.

buildRequestPayload δημιουργεί σωστό JSON με request_id, source_floor, destination_floor, callback_url, καταχωρεί το requestId και αλλάζει κατάσταση σε "WAITING_FOR_ELEVATOR".

Άκυρος προορισμός ή άκυρο requestId στο buildRequestPayload πετάει IllegalArgumentException και θέτει κατάσταση "INVALID_INPUT".

isKnownRequest επιστρέφει true μόνο για requestId που έχουν ήδη περάσει από buildRequestPayload.

formatDisplayMessage με έγκυρο elevatorId (A–T) επιστρέφει "Χρησιμοποιήστε τον ανελκυστήρα <id>" και θέτει κατάσταση "DISPLAYING_ASSIGNMENT", ενώ με άκυρο elevatorId πετάει IllegalArgumentException και θέτει "INVALID_INPUT".