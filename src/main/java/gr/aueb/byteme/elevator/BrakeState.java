package gr.aueb.byteme.elevator;

/**Φρένα ανελκυστήρα */ 
    public interface Brake {
        /**Επιστρέφει την κατάσταση των φρένων */
        BrakeState state();
        /**Απενεργοποιεί τα φρένα πριν κινηθεί ο ανελκυστήρας */
        void release();
        /**Ενεργοποιεί τα φρένα όταν σταματήσει ο ανελκυστήρας */
        void engage();
    }