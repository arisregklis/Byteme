package gr.aueb.byteme.central;
//Τι ξέρει το κεντρικό σύστημα για έναν ανελκυστήρα (όπως το GET / state).
public class ElevatorState {
    private String id; // "A", "B", "C"...
    private int floor; //σε ποιον όροφο είναι 
    private String status; // "IDLE", "MOVING", "STOPPED"
    private int stopsCount; // πόσες στάσεις έχει ακόμα να κάνει
    public ElevatorState(String id, int floor, String status, int stopsCount) {
        this.id = id;
        this.floor = floor;
        this.status = status;
        this.stopsCount = stopsCount;
    }
    public String getId() {
        return id;
    }
    public int getFloor() {
        return floor;
    }
    public String getStatus() {
        return status;
    }
    public int getStopsCount() {
        return stopsCount;
    }
}