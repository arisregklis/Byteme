package gr.aueb.byteme.elevator;

/** Προσομοίωση φρένων ανελκυστήρα */
public class SimulatedBrake implements Brake {
    /**Αρχική κατάσταση φρένων.Πρέπει να είναι ENGAGED */
    private BrakeState state = BrakeState.ENGAGED;
    /** Επέστρεψε την τρέχουσα κατάσταση των φρένων */
    @Override
    public BrakeState state() {
        return state;
    }
    /** Αν το φρένο είναι, πέτα new IllegalStateException("s6: brake failure") αλλιώς βάλε το state σε Released */
    @Override
    public void release() {
        if (state == BrakeState.FAILED) {
            throw new IllegalStateException("s6: brake failure");
        } else {
            state = BrakeState.RELEASED;
        }
    }
    /** Ενεργοποιεί τα φρένα όταν σταματήσει ο ανελκυστήρας αν δεν είναι FAILED */
    @Override
    public void engage() {
        if (state != BrakeState.FAILED) {
            state = BrakeState.ENGAGED;
        }
    }
    /** Για τα run test υπόθεση οτι τα φρένα αποτυγχάνουν */
    public void fail() {
        state = BrakeState.FAILED;
    }
}