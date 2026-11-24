# DISPATCH – Αλγόριθμος επιλογής ανελκυστήρα - V2

## 1. Σκοπός

Βασικος σκοπός είναι να φτιαξουμε ένα αρχειο το οποίο περιγραφει πως θα λειτουργήσει αρχικά το σύστημα μας με έναν απλό πρώτο αλγόριθμο! Με λίγα λόγια πρέπει να περιγραφει πως τρέχει τον αλγόριθμο πως επιλέγει τον ανελκυστήρα 

SimpleDispatcher είναι η κλάση που υλοποιεί την διεπαφή DispatchAlgorithm

## 2. Ποιοι ανελκυστήρες εξετάζονται

Εκτός μένουν όσοι είναι  STOPPED, STOPPING, EMERGENCY ή OUT_OF_SERVICE. και όσοι είχαν κάποιο error από  `GET /state` τον βάζουμε OUT_OF_SERVIVE

## 3. Κανόνας - αρχικός

priotiry = |floor - source_floor| + 6 × (πλήθος next_stops)

- |floor - source_floor|: πόσους ορόφους απέχει 
- 6 × next_stops: κάθε στάση που έχει να κάνει τον καθυστερεί
- σε περιπτωση ισοβαθμίας καλείται ο ανελκυστληρας με το μικρότερο γράμμα πχ 'A<C'

Επιλέγει το μικρότερο priority 

## 4 Ψευδογλώσσα

function chooseElevator(elevators, request):
    best = 0
    bestPriority = 10000

```
for each e in elevators:
        if e.status is STOPPED or STOPPING or EMERGENCY or OUT_OF_SERVICE:
        skip
    priority  = |e.floor - request.from| + 6 × count(e.stops)
        if priority < bestPriority:
        best = e.id
        bestpriority = priority 

return best
```

## 5. Παραδείγματα απο το 2 να κάνει τα εξης:

| Ανελκυστήρας | floor | status | next_stops | Priority |
|---|---|---|---|---|
| A | 0 | IDLE | [] | 2 + 0 = **2** |
| B | 5 | MOVING | [6, 9] | 3 + 12 = 15 |
| C | 2 | STOPPED | [] | εκτός |

Επιλογή: A→  Το κεντρικό απαντα 202`{ "request_id": "req-2-1", status : PENDING "elevator_id": "A" }`
 Μετά POST /display { "request_id": "req-2-1", "elevator_id": "A" } στο Kiosk.

Παράδειγμα 2 (ισοβαθμία). Αίτημα από τον 5. A στον 3, B στον 7, χωρίς στάσεις. Priority 2 και 2 → A.

Παράδειγμα 3.Όλοι STOPPED → κανένας → `503 { "error": "no elevator available" }`.

## 6. Διεπαφή Java

java
package gr.aueb.byteme.central;

import gr.aueb.byteme.common.Request;

public interface DispatchAlgorithm {
    // επιστρέφει το γράμμα του ανελκυστήρα ή null για το error 503
String chooseElevator(ElevatorState[] states, Request request);
}


## 7. Οριακές περιπτώσεις

| Περίπτωση | Συμπεριφορά |
|---|---|
| `source_floor == destination_floor` | `400 { "error": "source_floor and destination_floor must differ" }` |
| Κανένας διαθέσιμος | `503 { "error": "no elevator available" }` |


|Όροφος εκτός 0..floors−1|  400 INVALID_FLOOR | 
|Κενό request_id» | 400 MISSING_REQUEST_ID |
|Ίδιο request_id δεύτερη φορά|  409 DUPLICATE_REQUEST |
| «Ανελκυστήρας δεν απαντά» | μετράει ως OUT_OF_SERVICE