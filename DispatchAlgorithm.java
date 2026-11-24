package gr.aueb.byteme.central;
import docs.ElevatorState;
import gr.aueb.byteme.common.Request;
// Η διεπαφή του αλγορίθμου: παίρνει ανελκυστήρες και αίτημα, δίενι γράμμα.
public interface DispatchAlgorithm {
    //επιστρέφει το γράμμα του ανελκυστήρα ή nullαν δεν υπάρχει διαθέσιμος (503)
    String chooseElevator(ElevatorState[] states, Request request);
}