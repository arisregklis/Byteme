package gr.aueb.byteme.common;
// Επιβάτης κάνει αίτημα να πάει απο τον όροφο sourceFloor στον όροφο destinationFloor
public class Request {
    private int sourceFloor;
    private int destinationFloor;

    public Request(int sourceFloor, int destinationFloor) {
    this.sourceFloor = sourceFloor;
    this.destinationFloor = destinationFloor;
    }

    public int getSourceFloor() {
        return sourceFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }
}


