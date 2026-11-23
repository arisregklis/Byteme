package gr.aueb.byteme.elevator;

    /**Καταστάσεις φρένων */
    public enum BrakeState  {
        /**Τα φρένα είναι ενεργοποιημένα */
        ENGAGED,
        /**Τα φρένα είναι απενεργοποιημένα */
        RELEASED,
        /**Τα φρένα δεν λειτούργησαν με επιτυχία */
        FAILED 
    }