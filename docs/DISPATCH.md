# DISPATCH – Αλγόριθμος επιλογής ανελκυστήρα - V1

## 1. Σκοπός

Βασικος σκοπός είναι να φτιαξουμε ένα αρχειο το οποίο περιγραφει πως θα λειτουργήσει αρχικά το σύστημα μας με έναν απλό πρώτο αλγόριθμο! Με λίγα λόγια πρέπει να περιγραφει πως τρέχει τον αλγόριθμο πως επιλέγει τον ανελκυστήρα 

## 2. Ποιοι ανελκυστήρες εξετάζονται

Εκτός μένουν όσοι είναι STOPPED ή STOPPING και όσοι είχαν κάποιο error από  `GET /state`.

## 3. Κανόνας - αρχικός

priotiry = |floor - where_from| + 6 × (πλήθος next_stops)

- |floor - where_from|: πόσους ορόφους απέχει 
- 6 × next_stops: κάθε στάση που έχει να κάνει τον καθυστερεί

Επιλέγει το μικρότερο priority 

## 4 Ψευδογλώσσα

function chooseElevator(elevators, request):
    best = 0
    bestPriority = 10000

```
for each e in elevators:
    if e.status is STOPPED or STOPPING:
        skip
    priority  = |e.floor - request.from| + 6 × count(e.stops)
    if cost < bestCost:
        best = e
        bestpriority = priority 

return best
```

## 5. Παραδείγματα απο το 2 να κάνει τα εξης:

| Ανελκυστήρας | floor | status | next_stops | Priority |
|---|---|---|---|---|
| A | 0 | IDLE | [] | 2 + 0 = **2** |
| B | 5 | MOVING | [6, 9] | 3 + 12 = 15 |
| C | 2 | STOPPED | [] | εκτός |

Επιλογή: A→ `{ "request_id": 41, "elevator_id": "A" }`.

Παράδειγμα 2 (ισοβαθμία). Αίτημα από τον 5. A στον 3, B στον 7, χωρίς στάσεις. Priority 2 και 2 → A.

Παράδειγμα 3.Όλοι STOPPED → κανένας → `503 { "error": "no elevator available" }`.

## 6. Διεπαφή Java

java
package gr.aueb.elevators;

import java.util.List;

public interface DispatchAlgorithm {
    // επιστρέφει το γράμμα του ανελκυστήρα ή null για το error 503
    String chooseElevator(List<ElevatorState> states, Request request);
}


## 7. Οριακές περιπτώσεις

| Περίπτωση | Συμπεριφορά |
|---|---|
| `where_from == where_to` | `400 { "error": "where_from and where_to must differ" }` |
| Κανένας διαθέσιμος | `503 { "error": "no elevator available" }` |
