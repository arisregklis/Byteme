package gr.aueb.byteme.central;

import gr.aueb.byteme.common.Request;

// Η διεπαφή του αλγορίθμου: παίρνει ανελκυστήρες και αίτημα, δίνει γράμμα.
public interface DispatchAlgorithm {
    // επιστρέφει το γράμμα του ανελκυστήρα ή null αν δεν υπάρχει διαθέσιμος (503)
    String chooseElevator(ElevatorState[] states, Request request);
}