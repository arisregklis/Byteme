# Ασφάλεια Συστήματος Ελέγχου Ανελκυστήρα

## 1. Σκοπός

Το πρόγραμμα δεν αποτελεί πιστοποιημένο σύστημα ελέγχου πραγματικού ανελκυστήρα. Προσομοιώνει όμως βασικές λειτουργίες ασφαλείας, ώστε ο ανελκυστήρας να μεταβαίνει σε ασφαλή κατάσταση όταν εντοπίζεται πρόβλημα.

Ο βασικός κανόνας είναι:

> Αν δεν μπορεί να αποδειχθεί ότι η κίνηση είναι ασφαλής, ο κινητήρας δεν ξεκινά ή σταματά.

Οι εντολές ασφαλείας έχουν μεγαλύτερη προτεραιότητα από τις στάσεις που στέλνει το Κεντρικό Σύστημα.

---

## 2. Λειτουργίες ασφαλείας που θα υλοποιήσουμε

Από τα συστήματα ενός πραγματικού ανελκυστήρα, στην εφαρμογή μας θα προσομοιώσουμε:

1. Έλεγχο θυρών.
2. Έλεγχο εμποδίου στις θύρες.
3. Έλεγχο υπερφόρτωσης.
4. Έλεγχο ορίων ορόφων.
5. Έλεγχο θέσης και ακρίβειας στάσης.
6. Έλεγχο υπερβολικής ταχύτητας.
7. Έλεγχο λειτουργίας κινητήρα και φρένου.
8. Έλεγχο τερματικών διακοπτών.
9. Έλεγχο διακοπής ρεύματος.
10. Κουμπί έκτακτης ανάγκης και συναγερμό.
11. Κατάσταση πυρκαγιάς.
12. Κατάσταση συντήρησης.
13. Εσωτερικό κύκλωμα ασφαλείας.
14. Καταγραφή βλαβών και συμβάντων.
15. Ασφαλή επαναφορά μετά από βλάβη.

Δεν θα προσομοιώσουμε αναλυτικά μηχανικά εξαρτήματα, όπως συρματόσχοινα, τροχαλίες, υδραυλικές βαλβίδες και πραγματική αρπάγη ασφαλείας. Θα προσομοιώσουμε τα σήματα που θα έστελναν αυτά τα συστήματα στον ελεγκτή.

---

## 3. Καταστάσεις λειτουργίας

```java
public enum ElevatorStatus {
    IDLE,
    MOVING,
    DOORS_OPEN,
    STOPPING,
    STOPPED,
    EMERGENCY,
    FIRE_SERVICE,
    INSPECTION,
    OUT_OF_SERVICE,
    POWER_FAILURE
}
```

### Περιγραφή

| Κατάσταση | Σημασία |
|---|---|
| `IDLE` | Ακίνητος και διαθέσιμος |
| `MOVING` | Κινείται προς την επόμενη στάση |
| `DOORS_OPEN` | Έχει σταματήσει και οι θύρες είναι ανοιχτές |
| `STOPPING` | Εκτελεί ασφαλή ακινητοποίηση |
| `STOPPED` | Έχει σταματήσει μετά από εντολή |
| `EMERGENCY` | Υπάρχει κατάσταση έκτακτης ανάγκης |
| `FIRE_SERVICE` | Εκτελείται ειδική λειτουργία πυρκαγιάς |
| `INSPECTION` | Εκτελείται λειτουργία συντήρησης |
| `OUT_OF_SERVICE` | Έχει τεθεί εκτός λειτουργίας |
| `POWER_FAILURE` | Υπάρχει διακοπή κύριας τροφοδοσίας |

---

## 4. Καταστάσεις θυρών

```java
public enum DoorState {
    OPEN,
    OPENING,
    CLOSING,
    CLOSED_AND_LOCKED,
    BLOCKED,
    FAULT
}
```

Ο ανελκυστήρας μπορεί να κινηθεί μόνο όταν:

```java
doorState == DoorState.CLOSED_AND_LOCKED
```

Αν η πόρτα είναι ανοιχτή, ανοίγει, κλείνει, είναι μπλοκαρισμένη ή έχει βλάβη, η κίνηση απαγορεύεται.

---

## 5. Καταστάσεις κινητήρα και φρένου

```java
public enum MotorState {
    STOPPED,
    MOVING_UP,
    MOVING_DOWN,
    FAULT,
    OVERHEATED
}
```

```java
public enum BrakeState {
    ENGAGED,
    RELEASED,
    FAULT
}
```

Για να παραμένει ακίνητος ο θάλαμος, το φρένο πρέπει να είναι ενεργοποιημένο:

```text
motor_state = STOPPED
brake_state = ENGAGED
```

Για να ξεκινήσει η κίνηση:

- Οι θύρες πρέπει να είναι κλειστές και κλειδωμένες.
- Το κύκλωμα ασφαλείας πρέπει να είναι έγκυρο.
- Το φρένο δεν πρέπει να έχει βλάβη.
- Ο κινητήρας δεν πρέπει να έχει βλάβη ή υπερθέρμανση.

---

## 6. Σήματα ασφαλείας

Η κατάσταση των αισθητήρων μπορεί να αποθηκεύεται σε μία κλάση:

```java
public class SafetyState {
    private boolean doorClosed;
    private boolean doorLocked;
    private boolean doorBlocked;

    private boolean overloaded;
    private boolean emergencyButtonPressed;
    private boolean alarmActive;

    private boolean overspeedDetected;
    private boolean upperLimitReached;
    private boolean lowerLimitReached;

    private boolean motorFault;
    private boolean motorOverheated;
    private boolean brakeFault;

    private boolean positionSensorFault;
    private boolean safetyCircuitClosed;

    private boolean powerAvailable;
    private boolean fireAlarmActive;
    private boolean inspectionMode;
}
```

Στην αρχική κατάσταση:

```text
doorClosed = true
doorLocked = true
doorBlocked = false
overloaded = false
emergencyButtonPressed = false
alarmActive = false
overspeedDetected = false
upperLimitReached = false
lowerLimitReached = false
motorFault = false
motorOverheated = false
brakeFault = false
positionSensorFault = false
safetyCircuitClosed = true
powerAvailable = true
fireAlarmActive = false
inspectionMode = false
```

---

## 7. Κεντρικός έλεγχος πριν από την κίνηση

Όλοι οι κανόνες συγκεντρώνονται σε μία κλάση, ώστε να μη βρίσκονται διάσπαρτοι στον κώδικα.

```java
public final class SafetyController {

    public SafetyResult checkMovementAllowed(
            ElevatorState elevator,
            SafetyState safety) {

        if (!safety.isPowerAvailable()) {
            return SafetyResult.denied("POWER_FAILURE");
        }

        if (!safety.isSafetyCircuitClosed()) {
            return SafetyResult.denied("SAFETY_CIRCUIT_OPEN");
        }

        if (safety.isEmergencyButtonPressed()) {
            return SafetyResult.denied("EMERGENCY_ACTIVE");
        }

        if (safety.isFireAlarmActive()) {
            return SafetyResult.denied("FIRE_SERVICE_ACTIVE");
        }

        if (safety.isInspectionMode()) {
            return SafetyResult.denied("INSPECTION_MODE_ACTIVE");
        }

        if (!safety.isDoorClosed() || !safety.isDoorLocked()) {
            return SafetyResult.denied("DOOR_NOT_LOCKED");
        }

        if (safety.isDoorBlocked()) {
            return SafetyResult.denied("DOOR_BLOCKED");
        }

        if (safety.isOverloaded()) {
            return SafetyResult.denied("OVERLOAD");
        }

        if (safety.isOverspeedDetected()) {
            return SafetyResult.denied("OVERSPEED");
        }

        if (safety.isMotorFault()) {
            return SafetyResult.denied("MOTOR_FAULT");
        }

        if (safety.isMotorOverheated()) {
            return SafetyResult.denied("MOTOR_OVERHEATED");
        }

        if (safety.isBrakeFault()) {
            return SafetyResult.denied("BRAKE_FAULT");
        }

        if (safety.isPositionSensorFault()) {
            return SafetyResult.denied("POSITION_SENSOR_FAULT");
        }

        if (elevator.getStatus() == ElevatorStatus.STOPPING ||
            elevator.getStatus() == ElevatorStatus.STOPPED ||
            elevator.getStatus() == ElevatorStatus.EMERGENCY ||
            elevator.getStatus() == ElevatorStatus.OUT_OF_SERVICE ||
            elevator.getStatus() == ElevatorStatus.POWER_FAILURE) {

            return SafetyResult.denied("STATUS_DOES_NOT_ALLOW_MOVEMENT");
        }

        return SafetyResult.allowed();
    }
}
```

Το `SafetyResult` επιστρέφει αν επιτρέπεται η κίνηση και, αν όχι, τον λόγο απόρριψης.

```java
public record SafetyResult(boolean allowed, String reason) {

    public static SafetyResult allowed() {
        return new SafetyResult(true, null);
    }

    public static SafetyResult denied(String reason) {
        return new SafetyResult(false, reason);
    }
}
```

---

## 8. Έλεγχος θυρών

Πριν ξεκινήσει ο κινητήρας πρέπει να ισχύουν ταυτόχρονα:

```text
doorClosed = true
doorLocked = true
doorBlocked = false
doorState = CLOSED_AND_LOCKED
```

Αν υπάρχει εμπόδιο κατά το κλείσιμο:

1. Η πόρτα σταματά να κλείνει.
2. Ανοίγει ξανά.
3. Η κατάσταση γίνεται `BLOCKED`.
4. Ο κινητήρας δεν ξεκινά.
5. Καταγράφεται event `DOOR_BLOCKED`.

Παράδειγμα:

```java
if (safetyState.isDoorBlocked()) {
    motor.stop();
    doors.open();
    elevatorState.setStatus(ElevatorStatus.DOORS_OPEN);
    eventLogger.record("DOOR_BLOCKED");
}
```

Η πόρτα ορόφου δεν πρέπει να θεωρείται ότι ανοίγει αν ο θάλαμος δεν βρίσκεται στη σωστή ζώνη στάσης.

---

## 9. Έλεγχος βάρους

Στην προσομοίωση θα αποθηκεύουμε:

```java
private int currentWeightKg;
private int maximumWeightKg;
```

Έλεγχος:

```java
boolean overloaded = currentWeightKg > maximumWeightKg;
safetyState.setOverloaded(overloaded);
```

Αν υπάρχει υπερφόρτωση:

- Ο κινητήρας δεν ξεκινά.
- Οι θύρες παραμένουν ανοιχτές.
- Καταγράφεται event `OVERLOAD`.
- Το `GET /state` επιστρέφει `overloaded: true`.
- Ο ανελκυστήρας συνεχίζει μόνο όταν μειωθεί το βάρος.

Δεν χρειάζεται να προσομοιώσουμε υποφόρτωση στην πρώτη έκδοση, επειδή δεν είναι απαραίτητη για τη βασική λειτουργία.

---

## 10. Έλεγχος ορίων διαδρομής

Ο ανελκυστήρας δεν επιτρέπεται να κινηθεί:

- Πάνω από τον `max_floor`.
- Κάτω από τον `min_floor`.
- Προς τα πάνω όταν έχει ενεργοποιηθεί ο άνω τερματικός διακόπτης.
- Προς τα κάτω όταν έχει ενεργοποιηθεί ο κάτω τερματικός διακόπτης.

```java
if (direction == Direction.UP &&
    (currentFloor >= maxFloor || safetyState.isUpperLimitReached())) {

    emergencyStop("UPPER_LIMIT_REACHED");
}

if (direction == Direction.DOWN &&
    (currentFloor <= minFloor || safetyState.isLowerLimitReached())) {

    emergencyStop("LOWER_LIMIT_REACHED");
}
```

Η προσπάθεια εισαγωγής άκυρης στάσης επιστρέφει `400 Bad Request`.

---

## 11. Ακρίβεια στάσης

Οι θύρες επιτρέπεται να ανοίξουν μόνο όταν:

- Ο κινητήρας έχει σταματήσει.
- Το φρένο έχει ενεργοποιηθεί.
- Ο θάλαμος βρίσκεται στον όροφο προορισμού.
- Η διαφορά θέσης είναι μέσα στο επιτρεπόμενο όριο.

Για την προσομοίωση μπορούμε να χρησιμοποιήσουμε:

```java
private int positionErrorMm;
private int maximumLevelingErrorMm = 20;
```

Έλεγχος:

```java
boolean correctlyLeveled =
        Math.abs(positionErrorMm) <= maximumLevelingErrorMm;
```

Αν ο θάλαμος δεν βρίσκεται σωστά στο επίπεδο του ορόφου:

- Οι θύρες δεν ανοίγουν.
- Η κατάσταση γίνεται `OUT_OF_SERVICE`.
- Καταγράφεται event `LEVELING_FAULT`.

---

## 12. Έλεγχος ταχύτητας

Θα υπάρχει μέγιστη επιτρεπόμενη ταχύτητα:

```java
private double currentSpeedMps;
private double maximumSpeedMps;
```

Αν:

```java
currentSpeedMps > maximumSpeedMps
```

τότε:

1. Καταγράφεται `OVERSPEED`.
2. Ο κινητήρας σταματά.
3. Ενεργοποιείται το φρένο.
4. Η κατάσταση γίνεται `EMERGENCY`.
5. Ακυρώνεται η κανονική κίνηση.

Στην πρώτη έκδοση οι τιμές ταχύτητας μπορούν να είναι προσομοιωμένες.

---

## 13. Έλεγχος κινητήρα και φρένου

Αν παρουσιαστεί βλάβη στον κινητήρα:

- Ο κινητήρας σταματά.
- Το φρένο ενεργοποιείται.
- Η κατάσταση γίνεται `OUT_OF_SERVICE`.
- Καταγράφεται `MOTOR_FAULT`.

Αν ο κινητήρας υπερθερμανθεί:

- Δεν γίνονται δεκτές νέες διαδρομές.
- Ολοκληρώνεται μόνο η τρέχουσα κίνηση, αν αυτό είναι ασφαλές.
- Στη συνέχεια ο ανελκυστήρας σταματά και γίνεται προσωρινά μη διαθέσιμος.

Αν παρουσιαστεί βλάβη στο φρένο:

- Δεν επιτρέπεται νέα κίνηση.
- Η κατάσταση γίνεται `OUT_OF_SERVICE`.
- Καταγράφεται `BRAKE_FAULT`.

Για απλούστερη και ασφαλέστερη αρχική υλοποίηση μπορούμε να σταματάμε αμέσως και στην υπερθέρμανση.

---

## 14. Κύκλωμα ασφαλείας

Το `safetyCircuitClosed` αναπαριστά τη συνολική κατάσταση των κρίσιμων συστημάτων.

Θα είναι `true` μόνο όταν:

- Οι θύρες είναι ασφαλισμένες.
- Δεν έχει ενεργοποιηθεί τερματικός διακόπτης σε λάθος κατεύθυνση.
- Δεν υπάρχει emergency stop.
- Δεν υπάρχει σοβαρή βλάβη κινητήρα ή φρένου.
- Δεν υπάρχει σφάλμα κρίσιμου αισθητήρα.

Αν το κύκλωμα ανοίξει κατά την κίνηση:

```java
if (!safetyState.isSafetyCircuitClosed()) {
    emergencyStop("SAFETY_CIRCUIT_OPEN");
}
```

Ο έλεγχος πρέπει να γίνεται:

- Πριν ξεκινήσει η κίνηση.
- Πριν από κάθε αλλαγή ορόφου.
- Πριν ανοίξουν οι θύρες.
- Μετά από κάθε αλλαγή κατάστασης.

---

## 15. Έκτακτη ανάγκη

Η έκτακτη ανάγκη ενεργοποιείται όταν:

- Πατηθεί το emergency stop.
- Εντοπιστεί υπερβολική ταχύτητα.
- Ανοίξει το κύκλωμα ασφαλείας.
- Ενεργοποιηθεί τερματικός διακόπτης σε λάθος στιγμή.
- Εντοπιστεί επικίνδυνη κίνηση.
- Εμφανιστεί κρίσιμη βλάβη.

```java
public void emergencyStop(String reason) {
    motor.stop();
    brake.engage();
    state.setDirection(Direction.NONE);
    state.setStatus(ElevatorStatus.EMERGENCY);
    eventLogger.record("EMERGENCY", reason);
}
```

Σε κατάσταση `EMERGENCY`:

- Δεν γίνονται δεκτές νέες στάσεις.
- Το `POST /stops` επιστρέφει `409 Conflict`.
- Το απλό `POST /resume` δεν αρκεί.
- Πρέπει πρώτα να έχει αποκατασταθεί η αιτία του προβλήματος.

---

## 16. Διακοπή ρεύματος

Σε διακοπή κύριας τροφοδοσίας:

- Ο κινητήρας σταματά.
- Το φρένο ενεργοποιείται.
- Η κατάσταση γίνεται `POWER_FAILURE`.
- Η κανονική λίστα στάσεων παγώνει.
- Ενεργοποιείται ο φωτισμός και η επικοινωνία έκτακτης ανάγκης στην προσομοίωση.
- Καταγράφεται event `POWER_FAILURE`.

Στην πρώτη έκδοση δεν χρειάζεται να προσομοιώσουμε πραγματική εφεδρική μετακίνηση προς όροφο. Αρκεί η ασφαλής ακινητοποίηση και η ενημέρωση της κατάστασης.

---

## 17. Συναγερμός και εγκλωβισμός

Το πρόγραμμα θα διατηρεί:

```java
private boolean alarmActive;
private boolean emergencyCommunicationAvailable;
```

Με την ενεργοποίηση του συναγερμού:

- Καταγράφεται event `ALARM_ACTIVATED`.
- Η πληροφορία εμφανίζεται στο `GET /state`.
- Θεωρούμε ότι γίνεται προσπάθεια επικοινωνίας με το αρμόδιο κέντρο.

Δεν θα προσομοιώσουμε διαδικασία χειροκίνητου απεγκλωβισμού, επειδή αυτή πραγματοποιείται από εξουσιοδοτημένο τεχνικό και δεν αποτελεί κανονική εντολή του REST API.

---

## 18. Λειτουργία πυρκαγιάς

Όταν ενεργοποιηθεί το `fireAlarmActive`:

1. Δεν γίνονται δεκτά νέα κανονικά αιτήματα.
2. Η κατάσταση γίνεται `FIRE_SERVICE`.
3. Οι υπάρχουσες στάσεις ακυρώνονται ή παγώνουν.
4. Ο ανελκυστήρας κατευθύνεται σε προκαθορισμένο όροφο, μόνο αν η κίνηση είναι ασφαλής.
5. Όταν φτάσει, σταματά και ανοίγει τις θύρες.
6. Καταγράφεται event `FIRE_SERVICE_ACTIVATED`.

Ο προκαθορισμένος όροφος θα βρίσκεται στο configuration:

```json
{
  "fire_recall_floor": 0
}
```

Αυτή είναι προσομοιωμένη λειτουργία για τις ανάγκες της εργασίας και όχι πλήρης πραγματική λειτουργία πυρόσβεσης.

---

## 19. Λειτουργία επιθεώρησης

Όταν `inspectionMode = true`:

- Δεν εκτελούνται κανονικά αιτήματα από το Κεντρικό Σύστημα.
- Το `POST /stops` επιστρέφει `409 Conflict`.
- Η κατάσταση γίνεται `INSPECTION`.
- Επιτρέπονται μόνο ειδικές προσομοιωμένες εντολές συντήρησης.
- Η ταχύτητα θεωρείται περιορισμένη.

Στην πρώτη έκδοση δεν είναι απαραίτητο να υλοποιήσουμε ξεχωριστό REST endpoint για χειροκίνητη κίνηση συντήρησης. Αρκεί να απορρίπτονται οι κανονικές στάσεις.

---

## 20. Ασφαλής επαναφορά

Δεν επιτρέπεται αυτόματη επανεκκίνηση μετά από σοβαρή βλάβη.

Πριν από το `POST /resume` ελέγχονται:

- Το emergency stop έχει απενεργοποιηθεί.
- Το κύκλωμα ασφαλείας είναι κλειστό.
- Οι θύρες λειτουργούν.
- Δεν υπάρχει υπερφόρτωση.
- Δεν υπάρχει βλάβη κινητήρα.
- Δεν υπάρχει βλάβη φρένου.
- Δεν υπάρχει overspeed.
- Υπάρχει τροφοδοσία.
- Δεν είναι ενεργή η λειτουργία πυρκαγιάς.
- Δεν είναι ενεργή η λειτουργία επιθεώρησης.

Αν κάποιος έλεγχος αποτύχει:

```http
409 Conflict
```

```json
{
  "error": "elevator cannot resume safely",
  "reason": "BRAKE_FAULT"
}
```

---

## 21. Ενημερωμένη κατάσταση ανελκυστήρα

Το `GET /state` μπορεί να επιστρέφει:

```json
{
  "elevator_id": "C",
  "current_floor": 3,
  "direction": "UP",
  "status": "MOVING",
  "door_state": "CLOSED_AND_LOCKED",
  "motor_state": "MOVING_UP",
  "brake_state": "RELEASED",
  "next_stops": [5, 9],
  "current_weight_kg": 430,
  "maximum_weight_kg": 800,
  "current_speed_mps": 1.0,
  "maximum_speed_mps": 1.5,
  "overloaded": false,
  "overspeed": false,
  "emergency": false,
  "power_available": true,
  "fire_alarm_active": false,
  "inspection_mode": false,
  "safety_circuit_closed": true,
  "fault_code": null
}
```

Για την πρώτη έκδοση δεν είναι υποχρεωτικό να επιστραφούν όλα τα παραπάνω πεδία. Τα απολύτως απαραίτητα είναι:

```json
{
  "elevator_id": "C",
  "current_floor": 3,
  "direction": "UP",
  "status": "MOVING",
  "door_state": "CLOSED_AND_LOCKED",
  "next_stops": [5, 9],
  "overloaded": false,
  "emergency": false,
  "fault": false
}
```

---

## 22. Γεγονότα ασφάλειας

Το Σύστημα Αξιολόγησης μπορεί να λαμβάνει:

```text
DOOR_BLOCKED
DOOR_FAULT
OVERLOAD
OVERSPEED
UPPER_LIMIT_REACHED
LOWER_LIMIT_REACHED
LEVELING_FAULT
MOTOR_FAULT
MOTOR_OVERHEATED
BRAKE_FAULT
POSITION_SENSOR_FAULT
SAFETY_CIRCUIT_OPEN
EMERGENCY
POWER_FAILURE
POWER_RESTORED
ALARM_ACTIVATED
FIRE_SERVICE_ACTIVATED
INSPECTION_MODE_ACTIVATED
OUT_OF_SERVICE
RESUMED
```

Παράδειγμα:

```json
{
  "timestamp": "2026-11-23T13:30:00Z",
  "elevator_id": "C",
  "event": "OVERLOAD",
  "floor": 3,
  "details": {
    "current_weight_kg": 860,
    "maximum_weight_kg": 800
  }
}
```

---

## 23. Απαραίτητα tests ασφαλείας

### Θύρες

- Δεν ξεκινά με ανοιχτή πόρτα.
- Δεν ξεκινά με ξεκλείδωτη πόρτα.
- Δεν ξεκινά με μπλοκαρισμένη πόρτα.
- Ανοίγει ξανά την πόρτα όταν εντοπίζεται εμπόδιο.
- Δεν ανοίγει τις θύρες ανάμεσα σε ορόφους.

### Βάρος

- Ξεκινά όταν το βάρος είναι ίσο με το επιτρεπόμενο όριο.
- Δεν ξεκινά όταν το βάρος ξεπερνά το όριο.
- Συνεχίζει όταν το βάρος μειωθεί.
- Δημιουργεί event `OVERLOAD`.

### Όροφοι και θέση

- Απορρίπτει όροφο κάτω από το ελάχιστο.
- Απορρίπτει όροφο πάνω από το μέγιστο.
- Σταματά στον άνω τερματικό διακόπτη.
- Σταματά στον κάτω τερματικό διακόπτη.
- Δεν ανοίγει τις θύρες όταν η ισοστάθμιση είναι λανθασμένη.

### Κινητήρας και ταχύτητα

- Η υπερβολική ταχύτητα προκαλεί `EMERGENCY`.
- Η βλάβη κινητήρα προκαλεί `OUT_OF_SERVICE`.
- Η βλάβη φρένου απαγορεύει νέα κίνηση.
- Η υπερθέρμανση απαγορεύει νέα διαδρομή.

### Ηλεκτρική ασφάλεια

- Το ανοιχτό κύκλωμα ασφαλείας εμποδίζει την εκκίνηση.
- Το άνοιγμα του κυκλώματος κατά την κίνηση προκαλεί ασφαλή διακοπή.
- Η απώλεια ρεύματος οδηγεί σε `POWER_FAILURE`.
- Η επαναφορά του ρεύματος δεν προκαλεί αυτόματη κίνηση.

### Ειδικές καταστάσεις

- Το emergency stop ακυρώνει την κανονική λειτουργία.
- Το `EMERGENCY` απορρίπτει νέες στάσεις.
- Η λειτουργία πυρκαγιάς απορρίπτει κανονικές κλήσεις.
- Η λειτουργία επιθεώρησης απορρίπτει κανονικές στάσεις.
- Το `POST /resume` απορρίπτεται όσο υπάρχει βλάβη.
- Το `POST /resume` λειτουργεί όταν όλοι οι έλεγχοι είναι ασφαλείς.

---

## 24. Προτεραιότητες υλοποίησης

Δεν χρειάζεται να υλοποιηθούν όλα ταυτόχρονα.

### Πρώτη προτεραιότητα

Αυτά χρειάζονται οπωσδήποτε:

1. Έλεγχος κλειστής και κλειδωμένης πόρτας.
2. Έλεγχος εμποδίου πόρτας.
3. Έλεγχος ορίου βάρους.
4. Έλεγχος ορίων ορόφων.
5. `STOPPING` και `STOPPED`.
6. Emergency stop.
7. Κατάσταση `OUT_OF_SERVICE`.
8. Απαγόρευση αυτόματης επανεκκίνησης.
9. Κοινός `SafetyController`.
10. JUnit tests για όλους τους παραπάνω κανόνες.

### Δεύτερη προτεραιότητα

Αφού λειτουργήσουν τα βασικά:

1. Έλεγχος ταχύτητας.
2. Έλεγχος κινητήρα.
3. Έλεγχος φρένου.
4. Κύκλωμα ασφαλείας.
5. Απώλεια ρεύματος.
6. Έλεγχος ακρίβειας στάσης.
7. Καταγραφή αναλυτικών fault codes.

### Προαιρετικά

Αν υπάρχει χρόνος:

1. Λειτουργία πυρκαγιάς.
2. Λειτουργία επιθεώρησης.
3. Συναγερμός και επικοινωνία εγκλωβισμού.
4. Προσομοίωση υπερθέρμανσης.
5. Προσομοίωση εφεδρικής μετακίνησης.

---
